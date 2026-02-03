package org.umc.travlocksserver.domain.template.service.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDayRouteResponseDTO;
import org.umc.travlocksserver.domain.template.entity.MoveTime;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.MoveTimeRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.vlock.code.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.external.tmap.TmapApiService;
import org.umc.travlocksserver.global.external.tmap.TmapDTO;
import org.umc.travlocksserver.global.util.PolylineUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateRouteQueryService {

	private final MoveTimeRepository moveTimeRepository;
	private final TemplateDayRepository templateDayRepository;
	private final TemplateVlockRepository templateVlockRepository;
	private final VlockRepository vlockRepository;
	private final TmapApiService tmapApiService;
	private final PolylineUtil polylineUtil;

	@Transactional
	public List<TemplateDayRouteResponseDTO> getDayRoutes(
		Long templateId,
		Integer dayNo,
		TransportType transportType
	) {
		TemplateDay templateDay = templateDayRepository
			.findByTemplateIdAndDayNo(templateId, dayNo)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_DAY_NOT_FOUND));

		List<TemplateVlock> templateVlocks = templateVlockRepository
			.findByTemplateDayIdOrderByOrderNo(templateDay.getId());

		if (templateVlocks.size() < 2) {
			log.info("경로를 생성할 Vlock이 부족합니다. (최소 2개 필요)");
			return new ArrayList<>();
		}

		// 연속된 Vlock 쌍으로 경로 생성
		List<TemplateDayRouteResponseDTO> routes = new ArrayList<>();
		for (int i = 0; i < templateVlocks.size() - 1; i++) {
			TemplateVlock from = templateVlocks.get(i);
			TemplateVlock to = templateVlocks.get(i + 1);

			TemplateDayRouteResponseDTO route = getOrCreateRoute(
				from.getVlock().getId(),
				to.getVlock().getId(),
				transportType
			);
			routes.add(route);
		}

		return routes;
	}

	/**
	 * 단일 경로 조회/생성 (캐싱)
	 */
	@Transactional
	public TemplateDayRouteResponseDTO getOrCreateRoute(
		Long fromVlockId,
		Long toVlockId,
		TransportType transportType
	) {
		return moveTimeRepository
			.findByFromVlockIdAndToVlockIdAndTransportType(fromVlockId, toVlockId, transportType)
			.map(this::toResponseDTO)
			.orElseGet(() -> {
				log.info("경로 캐시 미스 - 새로 계산합니다: {} -> {} ({})",
					fromVlockId, toVlockId, transportType);
				return createAndSaveRoute(fromVlockId, toVlockId, transportType);
			});
	}

	/**
	 * 새로운 경로 저장
	 */
	private TemplateDayRouteResponseDTO createAndSaveRoute(
		Long fromVlockId,
		Long toVlockId,
		TransportType transportType
	) {
		Vlock fromVlock = vlockRepository.findById(fromVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.START_VLOCK_NOT_FOUND));

		Vlock toVlock = vlockRepository.findById(toVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.END_VLOCK_NOT_FOUND));

		// 이동 수단에 맞는 경로 계산
		TmapDTO.RouteInfo routeInfo = calculateRoute(fromVlock, toVlock, transportType);

		MoveTime moveTime = MoveTime.builder()
			.fromVlock(fromVlock)
			.toVlock(toVlock)
			.moveMinutes(routeInfo.getTotalTimeMinutes())
			.transportType(transportType)
			.distanceMeter(routeInfo.getTotalDistanceMeter())
			.polyline(routeInfo.getPolyline())
			.build();

		moveTimeRepository.save(moveTime);

		return toResponseDTO(moveTime);
	}

	/**
	 * 이동 수단별 경로 계산
	 * 현재는 도보(WALK)만 지원
	 */
	private TmapDTO.RouteInfo calculateRoute(
		Vlock fromVlock,
		Vlock toVlock,
		TransportType transportType
	) {
		return switch (transportType) {
			case WALK -> tmapApiService.getPedestrianRoute(
				fromVlock.getLongitude(),
				fromVlock.getLatitude(),
				toVlock.getLongitude(),
				toVlock.getLatitude()
			);
			case CAR, TRANSIT -> throw new TemplateException(TemplateErrorCode.UNSUPPORTED_TRANSPORT_TYPE);
		};
	}

	/**
	 * MoveTime 엔티티 DTO로 변환
	 */
	private TemplateDayRouteResponseDTO toResponseDTO(MoveTime moveTime) {
		return new TemplateDayRouteResponseDTO(
			moveTime.getId(),
			moveTime.getFromVlock().getId(),
			moveTime.getToVlock().getId(),
			moveTime.getMoveMinutes(),
			moveTime.getDistanceMeter(),
			moveTime.getTransportType(),
			polylineUtil.toCoordinates(moveTime.getPolyline())
		);
	}
}