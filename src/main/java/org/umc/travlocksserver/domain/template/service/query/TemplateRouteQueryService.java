package org.umc.travlocksserver.domain.template.service.query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.infra.redis.template.RouteCache;
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
import org.umc.travlocksserver.domain.template.service.command.RouteCalculationService;
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
	private final RouteCalculationService routeCalculationService;
	private final PolylineUtil polylineUtil;
	private final RouteCache routeCache;
	private static final double NEARBY_ROUTE_LAT_TOLERANCE = 0.001;
	private static final double NEARBY_ROUTE_LON_TOLERANCE = 0.001;
	private static final double SHORT_DISTANCE_THRESHOLD_KM = 0.3;

	public List<TemplateDayRouteResponseDTO> getDayRoutes(
		Long templateId,
		Integer dayNo) {
		TemplateDay templateDay = templateDayRepository
			.findByTemplateIdAndDayNo(templateId, dayNo)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_DAY_NOT_FOUND));

		TransportType transportType = templateDay.getTemplate().getTransportType();

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

			routes.add(getOrCreateRoute(
				from.getVlock().getId(),
				to.getVlock().getId(),
				transportType));
		}

		return routes;
	}

	/**
	 * Redis 캐시 조회 — 미스 시 DB/API 조회
	 */
	public TemplateDayRouteResponseDTO getOrCreateRoute(
		Long fromVlockId,
		Long toVlockId,
		TransportType transportType) {
		TemplateDayRouteResponseDTO cached = routeCache.get(fromVlockId, toVlockId, transportType);
		if (cached != null) {
			return cached;
		}

		TemplateDayRouteResponseDTO result = getOrCreateRouteInternal(fromVlockId, toVlockId, transportType);

		if (result.moveTimeId() != null) {
			routeCache.set(fromVlockId, toVlockId, transportType, result);
		}

		return result;
	}

	/**
	 * DB 경로 조회/생성
	 */
	private TemplateDayRouteResponseDTO getOrCreateRouteInternal(
		Long fromVlockId,
		Long toVlockId,
		TransportType transportType) {
		// 1. 유효한(만료 안 된) 경로
		Optional<MoveTime> valid = moveTimeRepository.findValidRoute(
			fromVlockId, toVlockId, transportType, LocalDateTime.now());
		if (valid.isPresent()) {
			return toResponseDTO(valid.get());
		}

		// 2. 만료된 경로 → 즉시 반환 + 백그라운드 갱신
		Optional<MoveTime> expired = moveTimeRepository.findAnyRoute(
			fromVlockId, toVlockId, transportType);
		if (expired.isPresent()) {
			log.info("만료된 경로 재사용, 백그라운드 갱신 트리거: {} -> {} ({})", fromVlockId, toVlockId, transportType);
			routeCalculationService.calculateAndSaveAsync(fromVlockId, toVlockId, transportType);
			return toResponseDTO(expired.get());
		}

		// 3. DB에 없음 → 동기 계산
		log.info("경로 없음 - 동기 계산: {} -> {} ({})", fromVlockId, toVlockId, transportType);
		return createAndSaveRoute(fromVlockId, toVlockId, transportType);
	}

	/**
	 * 좌표 검증 → 300m 이하 자체 추정 / 근처 경로 재사용 / 외부 API 동기 호출 순으로 경로 계산
	 */
	private TemplateDayRouteResponseDTO createAndSaveRoute(
		Long fromVlockId,
		Long toVlockId,
		TransportType transportType) {
		Vlock fromVlock = vlockRepository.findById(fromVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.START_VLOCK_NOT_FOUND));

		Vlock toVlock = vlockRepository.findById(toVlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.END_VLOCK_NOT_FOUND));

		// 좌표가 유효하지 않은 값이면(더미/범위 밖) 외부 API 호출 없이 fallback
		if (!isValidKoreaCoord(fromVlock.getLongitude(), fromVlock.getLatitude())
			|| !isValidKoreaCoord(toVlock.getLongitude(), toVlock.getLatitude())) {

			log.warn("유효하지 않은 좌표로 경로 계산 스킵: from=({}, {}), to=({}, {})",
				fromVlock.getLongitude(), fromVlock.getLatitude(),
				toVlock.getLongitude(), toVlock.getLatitude());

			return fallbackRoute(fromVlockId, toVlockId, transportType);
		}

		// 300m 이하 직선거리 → 외부 API 호출 없이 도보 속도 기반 자체 추정
		double distKm = GeoUtil.haversineKm(
			new LatLng(fromVlock.getLatitude(), fromVlock.getLongitude()),
			new LatLng(toVlock.getLatitude(), toVlock.getLongitude()));
		if (distKm < SHORT_DISTANCE_THRESHOLD_KM) {
			log.info("짧은 거리 자체 계산: {} -> {} ({}m)", fromVlockId, toVlockId, (int)(distKm * 1000));
			return toResponseDTO(
				routeCalculationService.calculateAndSaveShortDistance(fromVlock, toVlock, transportType, distKm));
		}

		// 정방향 근처 경로 재사용
		MoveTime reusableRoute = findReusableNearbyRoute(fromVlock, toVlock, transportType);
		if (reusableRoute != null) {
			log.info("Reusable nearby route cache hit: {} -> {} ({}) using moveTimeId={}",
				fromVlockId, toVlockId, transportType, reusableRoute.getId());
			return toNearbyRouteResponseDTO(fromVlock, toVlock, transportType, reusableRoute);
		}

		// 외부 API 동기 호출
		log.info("외부 API 동기 계산: {} -> {} ({})", fromVlockId, toVlockId, transportType);
		return toResponseDTO(routeCalculationService.calculateAndSave(fromVlockId, toVlockId, transportType));
	}

	/**
	 * 출발지/도착지가 유사한 기존 경로 재사용 (TRANSIT은 노선/환승 정보 영향을 받으므로 근처 경로 재사용 불가)
	 *
	 * 1. 정방향(A → B) 근처 경로를 우선 탐색
	 * 2. 없을 경우 WALK에 한해 역방향(B → A) 경로도 재사용 허용
	 */
	private MoveTime findReusableNearbyRoute(
		Vlock fromVlock,
		Vlock toVlock,
		TransportType transportType) {
		// TRANSIT 근처 경로 재사용 불가
		if (transportType == TransportType.TRANSIT) {
			return null;
		}

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

		// 역방향(B→A) 근처 경로 재사용은 WALK만 허용
		if (transportType != TransportType.WALK) {
			return null;
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

	/**
	 * 근처 경로 재사용 — DB 저장 없이 DTO 반환
	 */
	private TemplateDayRouteResponseDTO toNearbyRouteResponseDTO(
		Vlock fromVlock,
		Vlock toVlock,
		TransportType transportType,
		MoveTime reusableRoute) {
		return new TemplateDayRouteResponseDTO(
			null,
			fromVlock.getId(),
			toVlock.getId(),
			reusableRoute.getMoveMinutes(),
			reusableRoute.getDistanceMeter(),
			transportType,
			polylineUtil.toCoordinates(reusableRoute.getPolyline()));
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
