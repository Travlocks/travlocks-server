package org.umc.travlocksserver.domain.template.service.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
import org.umc.travlocksserver.domain.template.service.command.RouteCalculationAsyncService;
import org.umc.travlocksserver.domain.vlock.code.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.geo.GeoUtil;
import org.umc.travlocksserver.global.geo.LatLng;
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
	private final RouteCalculationAsyncService routeCalculationAsyncService;
	private final PolylineUtil polylineUtil;

	@Autowired
	@Lazy
	private TemplateRouteQueryService self;

	private static final double NEARBY_ROUTE_LAT_TOLERANCE = 0.001;
	private static final double NEARBY_ROUTE_LON_TOLERANCE = 0.001;
	private static final double SHORT_DISTANCE_THRESHOLD_KM = 0.3; // 300m 이하는 자체 계산
	private static final double WALK_SPEED_M_PER_MIN = 80.0;
	private static final double WALK_DETOUR_FACTOR = 1.3;           // 직선거리 → 실보행거리 보정

	@Transactional
	public List<TemplateDayRouteResponseDTO> getDayRoutes(
		Long templateId,
		Integer dayNo,
		TransportType transportType) {
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

			TemplateDayRouteResponseDTO route = self.getOrCreateRoute(
				from.getVlock().getId(),
				to.getVlock().getId(),
				transportType);
			routes.add(route);
		}

		return routes;
	}

	/**
	 * 단일 경로 조회/생성
	 * - 인메모리 캐시(@Cacheable) → DB 정확 매칭 → 근처 경로 재사용(정방향+역방향)
	 *   → 짧은 거리 자체 계산 → TMap 비동기 + fallback 반환 순으로 처리
	 */
	@Cacheable(
		value = "moveTime",
		key = "#fromVlockId + '_' + #toVlockId + '_' + #transportType.name()",
		unless = "#result.moveTimeId == null"
	)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public TemplateDayRouteResponseDTO getOrCreateRoute(
		Long fromVlockId,
		Long toVlockId,
		TransportType transportType) {
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
		TransportType transportType) {
		Vlock fromVlock = vlockRepository.findById(fromVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.START_VLOCK_NOT_FOUND));

		Vlock toVlock = vlockRepository.findById(toVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.END_VLOCK_NOT_FOUND));

		// 좌표가 유효하지 않은 값이면(더미/범위 밖) TMAP 호출 없이 fallback
		if (!isValidKoreaCoord(fromVlock.getLongitude(), fromVlock.getLatitude())
			|| !isValidKoreaCoord(toVlock.getLongitude(), toVlock.getLatitude())) {

			log.warn("유효하지 않은 좌표로 경로 계산 스킵: from=({}, {}), to=({}, {})",
				fromVlock.getLongitude(), fromVlock.getLatitude(),
				toVlock.getLongitude(), toVlock.getLatitude());

			return fallbackRoute(fromVlockId, toVlockId, transportType);
		}

		// 300m 이하 직선거리 → TMap 호출 없이 도보 속도 기반 자체 추정
		double distKm = GeoUtil.haversineKm(
			new LatLng(fromVlock.getLatitude(), fromVlock.getLongitude()),
			new LatLng(toVlock.getLatitude(), toVlock.getLongitude()));
		if (distKm < SHORT_DISTANCE_THRESHOLD_KM) {
			log.info("짧은 거리 자체 계산: {} -> {} ({}m)", fromVlockId, toVlockId, (int)(distKm * 1000));
			return saveShortDistanceRoute(fromVlock, toVlock, transportType, distKm);
		}

		// 정방향 근처 경로 재사용
		MoveTime reusableRoute = findReusableNearbyRoute(fromVlock, toVlock, transportType);
		if (reusableRoute != null) {
			log.info("Reusable nearby route cache hit: {} -> {} ({}) using moveTimeId={}",
				fromVlockId, toVlockId, transportType, reusableRoute.getId());
			return saveRouteFromReusableCache(fromVlock, toVlock, transportType, reusableRoute);
		}

		// 캐시 미스 → TMap을 비동기로 계산하고 즉시 fallback 반환
		// 다음 호출 시 DB에 저장된 결과를 사용
		log.info("TMap 비동기 계산 트리거: {} -> {} ({})", fromVlockId, toVlockId, transportType);
		routeCalculationAsyncService.calculateAndSaveAsync(fromVlockId, toVlockId, transportType);
		return fallbackRoute(fromVlockId, toVlockId, transportType);
	}

	/**
	 * 정방향 근처 경로를 먼저 찾고, 없으면 역방향도 확인
	 */
	private MoveTime findReusableNearbyRoute(
		Vlock fromVlock,
		Vlock toVlock,
		TransportType transportType) {
		Optional<MoveTime> forward = moveTimeRepository.findTopReusableNearbyRoute(
			fromVlock.getLatitude(),
			fromVlock.getLongitude(),
			toVlock.getLatitude(),
			toVlock.getLongitude(),
			NEARBY_ROUTE_LAT_TOLERANCE,
			NEARBY_ROUTE_LON_TOLERANCE,
			transportType);

		if (forward.isPresent()) {
			return forward.get();
		}

		// 역방향(B→A) 근처 경로도 재활용 시도
		return moveTimeRepository.findTopReusableNearbyRouteReverse(
				fromVlock.getLatitude(),
				fromVlock.getLongitude(),
				toVlock.getLatitude(),
				toVlock.getLongitude(),
				NEARBY_ROUTE_LAT_TOLERANCE,
				NEARBY_ROUTE_LON_TOLERANCE,
				transportType)
			.orElse(null);
	}

	private TemplateDayRouteResponseDTO saveShortDistanceRoute(
		Vlock fromVlock,
		Vlock toVlock,
		TransportType transportType,
		double distKm) {
		int estimatedDistanceMeter = (int)Math.round(distKm * 1000 * WALK_DETOUR_FACTOR);
		int estimatedMinutes = (int)Math.ceil(estimatedDistanceMeter / WALK_SPEED_M_PER_MIN);

		MoveTime moveTime = MoveTime.builder()
			.fromVlock(fromVlock)
			.toVlock(toVlock)
			.moveMinutes(estimatedMinutes)
			.transportType(transportType)
			.distanceMeter(estimatedDistanceMeter)
			.polyline(String.format("[[%.6f,%.6f],[%.6f,%.6f]]",
				fromVlock.getLongitude(), fromVlock.getLatitude(),
				toVlock.getLongitude(), toVlock.getLatitude()))
			.build();

		moveTimeRepository.save(moveTime);
		return toResponseDTO(moveTime);
	}

	private TemplateDayRouteResponseDTO saveRouteFromReusableCache(
		Vlock fromVlock,
		Vlock toVlock,
		TransportType transportType,
		MoveTime reusableRoute) {
		MoveTime moveTime = MoveTime.builder()
			.fromVlock(fromVlock)
			.toVlock(toVlock)
			.moveMinutes(reusableRoute.getMoveMinutes())
			.transportType(transportType)
			.distanceMeter(reusableRoute.getDistanceMeter())
			.polyline(reusableRoute.getPolyline())
			.build();

		moveTimeRepository.save(moveTime);
		return toResponseDTO(moveTime);
	}

	private TemplateDayRouteResponseDTO fallbackRoute(Long fromVlockId, Long toVlockId, TransportType transportType) {
		return new TemplateDayRouteResponseDTO(null, fromVlockId, toVlockId, 0, 0, transportType, List.of());
	}

	/**
	 * 한국 내 좌표 검증
	 */
	private boolean isValidKoreaCoord(Double lon, Double lat) {
		if (lon == null || lat == null)
			return false;
		// 위경도 기본 범위
		if (lon < -180 || lon > 180 || lat < -90 || lat > 90)
			return false;
		// 한국 대략 범위
		return (lon >= 124.0 && lon <= 132.0) && (lat >= 33.0 && lat <= 39.5);
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
			polylineUtil.toCoordinates(moveTime.getPolyline()));
	}
}
