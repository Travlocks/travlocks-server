package org.umc.travlocksserver.domain.template.service.command;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.entity.MoveTime;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.MoveTimeRepository;
import org.umc.travlocksserver.domain.vlock.code.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.external.tmap.TmapApiService;
import org.umc.travlocksserver.global.external.tmap.TmapDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteCalculationAsyncService {

	private final Set<String> inProgress = ConcurrentHashMap.newKeySet();

	private final MoveTimeRepository moveTimeRepository;
	private final VlockRepository vlockRepository;
	private final TmapApiService tmapApiService;

	/**
	 * TMap 경로를 비동기로 계산해 DB에 저장
	 * 동일한 쌍에 대해 중복 호출이 들어오면 첫 번째만 실행하고 나머지는 스킵
	 */
	@Async
	@Transactional
	public void calculateAndSaveAsync(Long fromVlockId, Long toVlockId, TransportType transportType) {
		String key = fromVlockId + "_" + toVlockId + "_" + transportType;
		if (!inProgress.add(key)) {
			log.debug("이미 계산 중인 경로 스킵: {}", key);
			return;
		}
		try {
			if (moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(
				fromVlockId, toVlockId, transportType).isPresent()) {
				log.debug("비동기 계산 도중 이미 저장됨, 스킵: {}", key);
				return;
			}

			Vlock fromVlock = vlockRepository.findById(fromVlockId)
				.orElseThrow(() -> new VlockException(VlockErrorCode.START_VLOCK_NOT_FOUND));
			Vlock toVlock = vlockRepository.findById(toVlockId)
				.orElseThrow(() -> new VlockException(VlockErrorCode.END_VLOCK_NOT_FOUND));

			TmapDTO.RouteInfo routeInfo = switch (transportType) {
				case WALK -> tmapApiService.getPedestrianRoute(
					fromVlock.getLongitude(), fromVlock.getLatitude(),
					toVlock.getLongitude(), toVlock.getLatitude());
				case CAR, TRANSIT -> throw new TemplateException(TemplateErrorCode.UNSUPPORTED_TRANSPORT_TYPE);
			};

			moveTimeRepository.save(MoveTime.builder()
				.fromVlock(fromVlock)
				.toVlock(toVlock)
				.moveMinutes(routeInfo.getTotalTimeMinutes())
				.transportType(transportType)
				.distanceMeter(routeInfo.getTotalDistanceMeter())
				.polyline(routeInfo.getPolyline())
				.build());

			log.info("비동기 경로 계산 완료: {}", key);
		} catch (Exception e) {
			log.warn("비동기 경로 계산 실패: {} - {}", key, e.getMessage());
		} finally {
			inProgress.remove(key);
		}
	}
}
