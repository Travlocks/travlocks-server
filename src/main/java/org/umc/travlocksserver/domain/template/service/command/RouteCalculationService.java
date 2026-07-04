package org.umc.travlocksserver.domain.template.service.command;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.entity.MoveTime;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.repository.MoveTimeRepository;
import org.umc.travlocksserver.domain.vlock.code.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.external.odsay.OdsayApiService;
import org.umc.travlocksserver.global.external.tmap.TmapApiService;
import org.umc.travlocksserver.global.external.tmap.TmapDTO;
import org.umc.travlocksserver.infra.redis.template.RouteCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteCalculationService {

	static final double WALK_SPEED_M_PER_MIN = 80.0;
	static final double WALK_DETOUR_FACTOR = 1.3;

	private final Set<String> inProgress = ConcurrentHashMap.newKeySet();

	private final MoveTimeRepository moveTimeRepository;
	private final VlockRepository vlockRepository;
	private final TmapApiService tmapApiService;
	private final OdsayApiService odsayApiService;
	private final RouteCache routeCache;

	/**
	 * 경로를 동기로 계산해 DB에 저장하고 엔티티 반환 (WALK/CAR: TMap, TRANSIT: ODsay)
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MoveTime calculateAndSave(Long fromVlockId, Long toVlockId, TransportType transportType) {
		Vlock fromVlock = vlockRepository.findById(fromVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.START_VLOCK_NOT_FOUND));
		Vlock toVlock = vlockRepository.findById(toVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.END_VLOCK_NOT_FOUND));

		TmapDTO.RouteInfo routeInfo = switch (transportType) {
			case WALK -> tmapApiService.getPedestrianRoute(
				fromVlock.getLongitude(), fromVlock.getLatitude(),
				toVlock.getLongitude(), toVlock.getLatitude());
			case CAR -> tmapApiService.getCarRoute(
				fromVlock.getLongitude(), fromVlock.getLatitude(),
				toVlock.getLongitude(), toVlock.getLatitude());
			case TRANSIT -> odsayApiService.getTransitRoute(
				fromVlock.getLongitude(), fromVlock.getLatitude(),
				toVlock.getLongitude(), toVlock.getLatitude());
		};

		LocalDateTime expiresAt = switch (transportType) {
			case WALK -> null;
			case CAR -> LocalDateTime.now().plusDays(7);
			case TRANSIT -> LocalDateTime.now().plusDays(1);
		};

		return moveTimeRepository.save(MoveTime.builder()
			.fromVlock(fromVlock)
			.toVlock(toVlock)
			.moveMinutes(routeInfo.getTotalTimeMinutes())
			.transportType(transportType)
			.distanceMeter(routeInfo.getTotalDistanceMeter())
			.polyline(routeInfo.getPolyline())
			.expiresAt(expiresAt)
			.build());
	}

	/**
	 * ODsay 실패 시 TMap 도보 경로를 TRANSIT 타입으로 저장 (1일 만료 → 다음 날 ODsay 재시도)
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MoveTime calculateAndSaveTransitFallback(Long fromVlockId, Long toVlockId) {
		Vlock fromVlock = vlockRepository.findById(fromVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.START_VLOCK_NOT_FOUND));
		Vlock toVlock = vlockRepository.findById(toVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.END_VLOCK_NOT_FOUND));

		TmapDTO.RouteInfo routeInfo = tmapApiService.getPedestrianRoute(
			fromVlock.getLongitude(), fromVlock.getLatitude(),
			toVlock.getLongitude(), toVlock.getLatitude());

		return moveTimeRepository.save(MoveTime.builder()
			.fromVlock(fromVlock)
			.toVlock(toVlock)
			.moveMinutes(routeInfo.getTotalTimeMinutes())
			.transportType(TransportType.TRANSIT)
			.distanceMeter(routeInfo.getTotalDistanceMeter())
			.polyline(routeInfo.getPolyline())
			.expiresAt(LocalDateTime.now().plusDays(1))
			.build());
	}

	/**
	 * 300m 이하 직선거리 도보 속도 기반 자체 추정 저장
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MoveTime calculateAndSaveShortDistance(
		Vlock fromVlock,
		Vlock toVlock,
		TransportType transportType,
		double distKm) {
		int estimatedDistanceMeter = (int)Math.round(distKm * 1000 * WALK_DETOUR_FACTOR);
		int estimatedMinutes = (int)Math.ceil(estimatedDistanceMeter / WALK_SPEED_M_PER_MIN);

		return moveTimeRepository.save(MoveTime.builder()
			.fromVlock(fromVlock)
			.toVlock(toVlock)
			.moveMinutes(estimatedMinutes)
			.transportType(transportType)
			.distanceMeter(estimatedDistanceMeter)
			.polyline(String.format("[[%.6f,%.6f],[%.6f,%.6f]]",
				fromVlock.getLongitude(), fromVlock.getLatitude(),
				toVlock.getLongitude(), toVlock.getLatitude()))
			.build());
	}

	/**
	 * 경로를 비동기로 계산해 DB에 저장
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
			if (moveTimeRepository.findValidRoute(
				fromVlockId, toVlockId, transportType, LocalDateTime.now()).isPresent()) {
				log.debug("비동기 계산 도중 이미 저장됨, 스킵: {}", key);
				return;
			}
			calculateAndSave(fromVlockId, toVlockId, transportType);
			routeCache.evict(fromVlockId, toVlockId, transportType);
			log.info("비동기 경로 계산 완료 (캐시 evict): {}_{}_{}", fromVlockId, toVlockId, transportType);
		} catch (Exception e) {
			log.warn("비동기 경로 계산 실패: {}_{}_{} - {}", fromVlockId, toVlockId, transportType, e.getMessage());
		} finally {
			inProgress.remove(key);
		}
	}
}
