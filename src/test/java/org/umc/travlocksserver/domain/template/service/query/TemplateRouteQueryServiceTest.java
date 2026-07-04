package org.umc.travlocksserver.domain.template.service.query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDayRouteResponseDTO;
import org.umc.travlocksserver.domain.template.entity.MoveTime;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.repository.MoveTimeRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.template.service.command.RouteCalculationService;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.util.PolylineUtil;
import org.umc.travlocksserver.infra.redis.template.RouteCache;

@DisplayName("TemplateRouteQueryService unit tests")
@ExtendWith(MockitoExtension.class)
class TemplateRouteQueryServiceTest {

	@Mock
	MoveTimeRepository moveTimeRepository;
	@Mock
	TemplateDayRepository templateDayRepository;
	@Mock
	TemplateVlockRepository templateVlockRepository;
	@Mock
	VlockRepository vlockRepository;
	@Mock
	RouteCalculationService routeCalculationService;
	@Mock
	PolylineUtil polylineUtil;
	@Mock
	RouteCache routeCache;

	@InjectMocks
	TemplateRouteQueryService sut;

	private static final Long FROM = 1L;
	private static final Long TO = 2L;
	private static final TransportType WALK = TransportType.WALK;

	// ──────────────────────────────────────────────────────────────
	// 1. Redis 캐시 히트
	// ──────────────────────────────────────────────────────────────

	@Test
	@DisplayName("Returns immediately without querying the DB on Redis cache hit")
	void cacheHit_returnsImmediately() {
		TemplateDayRouteResponseDTO cached = new TemplateDayRouteResponseDTO(
			10L, FROM, TO, 5, 400, WALK, List.of());
		when(routeCache.get(FROM, TO, WALK)).thenReturn(cached);

		TemplateDayRouteResponseDTO result = sut.getOrCreateRoute(FROM, TO, WALK);

		assertThat(result).isEqualTo(cached);
		verifyNoInteractions(moveTimeRepository, vlockRepository, routeCalculationService);
	}

	// ──────────────────────────────────────────────────────────────
	// 2. 만료된 경로 → 즉시 반환 + 백그라운드 갱신
	// ──────────────────────────────────────────────────────────────

	@Test
	@DisplayName("Returns an expired route immediately and triggers background refresh")
	void expiredRoute_returnsAndTriggersAsyncRefresh() {
		MoveTime expired = moveTime(5L, FROM, TO, 8, 650, "[[127.0,37.5]]");
		when(routeCache.get(FROM, TO, WALK)).thenReturn(null);
		when(moveTimeRepository.findValidRoute(eq(FROM), eq(TO), eq(WALK), any())).thenReturn(Optional.empty());
		when(moveTimeRepository.findAnyRoute(FROM, TO, WALK)).thenReturn(Optional.of(expired));
		when(polylineUtil.toCoordinates("[[127.0,37.5]]")).thenReturn(List.of(List.of(127.0, 37.5)));

		TemplateDayRouteResponseDTO result = sut.getOrCreateRoute(FROM, TO, WALK);

		assertThat(result.moveTimeId()).isEqualTo(5L);
		assertThat(result.moveMinutes()).isEqualTo(8);
		assertThat(result.distanceMeter()).isEqualTo(650);
		verify(routeCalculationService).calculateAndSaveAsync(FROM, TO, WALK);
		verify(routeCache).set(eq(FROM), eq(TO), eq(WALK), any());
	}

	// ──────────────────────────────────────────────────────────────
	// 3. 유효한 DB 경로 → 재사용
	// ──────────────────────────────────────────────────────────────

	@Test
	@DisplayName("Reuses a valid DB route without Vlock lookup, save, or async calculation")
	void validDbRoute_reusesWithoutVlockOrAsyncCall() {
		MoveTime valid = moveTime(3L, FROM, TO, 10, 800, "[]");
		when(routeCache.get(FROM, TO, WALK)).thenReturn(null);
		when(moveTimeRepository.findValidRoute(eq(FROM), eq(TO), eq(WALK), any())).thenReturn(Optional.of(valid));
		when(polylineUtil.toCoordinates("[]")).thenReturn(List.of());

		TemplateDayRouteResponseDTO result = sut.getOrCreateRoute(FROM, TO, WALK);

		assertThat(result.moveTimeId()).isEqualTo(3L);
		assertThat(result.moveMinutes()).isEqualTo(10);
		verifyNoInteractions(vlockRepository);
		verify(moveTimeRepository, never()).findAnyRoute(any(), any(), any());
		verifyNoInteractions(routeCalculationService);
		verify(routeCache).set(FROM, TO, WALK, result);
	}

	// ──────────────────────────────────────────────────────────────
	// 4. 근처 경로 재사용
	// ──────────────────────────────────────────────────────────────

	@Test
	@DisplayName("Copies move time, distance, and polyline from a nearby route")
	void nearbyRoute_copiesTimeDistanceAndPolyline() {
		// 한국 내 좌표, 두 지점 간 거리 > 300m
		Vlock from = vlock(FROM, 37.5000, 127.0000);
		Vlock to = vlock(TO, 37.5040, 127.0040);
		List<List<Double>> coords = List.of(List.of(127.0, 37.5), List.of(127.004, 37.504));
		MoveTime nearby = moveTime(7L, 10L, 11L, 12, 950, "[[127.0,37.5],[127.004,37.504]]");

		when(routeCache.get(FROM, TO, WALK)).thenReturn(null);
		when(moveTimeRepository.findValidRoute(eq(FROM), eq(TO), eq(WALK), any())).thenReturn(Optional.empty());
		when(moveTimeRepository.findAnyRoute(FROM, TO, WALK)).thenReturn(Optional.empty());
		when(vlockRepository.findById(FROM)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(TO)).thenReturn(Optional.of(to));
		when(moveTimeRepository.findTopReusableNearbyRoute(
			anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(WALK)))
			.thenReturn(Optional.of(nearby));
		when(polylineUtil.toCoordinates(nearby.getPolyline())).thenReturn(coords);

		TemplateDayRouteResponseDTO result = sut.getOrCreateRoute(FROM, TO, WALK);

		assertThat(result.moveTimeId()).isNull(); // DB 저장 없이 DTO 직접 반환
		assertThat(result.fromVlockId()).isEqualTo(FROM);
		assertThat(result.toVlockId()).isEqualTo(TO);
		assertThat(result.moveMinutes()).isEqualTo(12);
		assertThat(result.distanceMeter()).isEqualTo(950);
		assertThat(result.polyline()).isEqualTo(coords);
		verify(routeCalculationService, never()).calculateAndSave(any(), any(), any());
		verify(routeCache, never()).set(any(), any(), any(), any()); // moveTimeId == null 이므로 캐시 저장 없음
	}

	// ──────────────────────────────────────────────────────────────
	// 5. 300m 이하 직선거리 → 자체 계산
	// ──────────────────────────────────────────────────────────────

	@Test
	@DisplayName("Self-calculates routes within 300 meters without external API")
	void shortDistance_selfCalculatesWithoutExternalApi() {
		// 두 지점 간 거리 < 300m (약 57m)
		Vlock from = vlock(FROM, 37.5000, 127.0000);
		Vlock to = vlock(TO, 37.5005, 127.0005);
		MoveTime shortResult = moveTime(9L, FROM, TO, 2, 130, "[[127.0,37.5],[127.0005,37.5005]]");

		when(routeCache.get(FROM, TO, WALK)).thenReturn(null);
		when(moveTimeRepository.findValidRoute(eq(FROM), eq(TO), eq(WALK), any())).thenReturn(Optional.empty());
		when(moveTimeRepository.findAnyRoute(FROM, TO, WALK)).thenReturn(Optional.empty());
		when(vlockRepository.findById(FROM)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(TO)).thenReturn(Optional.of(to));
		when(routeCalculationService.calculateAndSaveShortDistance(eq(from), eq(to), eq(WALK), anyDouble()))
			.thenReturn(shortResult);
		when(polylineUtil.toCoordinates(shortResult.getPolyline())).thenReturn(List.of());

		TemplateDayRouteResponseDTO result = sut.getOrCreateRoute(FROM, TO, WALK);

		assertThat(result.moveTimeId()).isEqualTo(9L);
		verify(routeCalculationService).calculateAndSaveShortDistance(eq(from), eq(to), eq(WALK), anyDouble());
		verify(routeCalculationService, never()).calculateAndSave(any(), any(), any());
		verify(moveTimeRepository, never()).findTopReusableNearbyRoute(
			anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
	}

	// ──────────────────────────────────────────────────────────────
	// 6. DB에 경로 없음 → 외부 API 동기 호출
	// ──────────────────────────────────────────────────────────────

	@Test
	@DisplayName("Calls the external API synchronously when no DB route exists")
	void noDbRoute_callsExternalApiSynchronously() {
		// 두 지점 간 거리 > 300m, 근처 경로도 없음
		Vlock from = vlock(FROM, 37.5000, 127.0000);
		Vlock to = vlock(TO, 37.5040, 127.0040);
		MoveTime apiResult = moveTime(20L, FROM, TO, 15, 1200, "[]");

		when(routeCache.get(FROM, TO, WALK)).thenReturn(null);
		when(moveTimeRepository.findValidRoute(eq(FROM), eq(TO), eq(WALK), any())).thenReturn(Optional.empty());
		when(moveTimeRepository.findAnyRoute(FROM, TO, WALK)).thenReturn(Optional.empty());
		when(vlockRepository.findById(FROM)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(TO)).thenReturn(Optional.of(to));
		when(moveTimeRepository.findTopReusableNearbyRoute(
			anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(WALK)))
			.thenReturn(Optional.empty());
		// WALK는 역방향도 탐색
		when(moveTimeRepository.findTopReusableNearbyRouteReverse(
			anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(WALK)))
			.thenReturn(Optional.empty());
		when(routeCalculationService.calculateAndSave(FROM, TO, WALK)).thenReturn(apiResult);
		when(polylineUtil.toCoordinates("[]")).thenReturn(List.of());

		TemplateDayRouteResponseDTO result = sut.getOrCreateRoute(FROM, TO, WALK);

		assertThat(result.moveTimeId()).isEqualTo(20L);
		verify(routeCalculationService).calculateAndSave(FROM, TO, WALK);
	}

	// ──────────────────────────────────────────────────────────────
	// 헬퍼
	// ──────────────────────────────────────────────────────────────

	/** 좌표가 세팅된 Vlock 목 생성
	 *  getId()는 경로에 따라 호출되지 않을 수 있으므로 lenient 처리 */
	private Vlock vlock(Long id, double lat, double lon) {
		Vlock v = mock(Vlock.class);
		lenient().when(v.getId()).thenReturn(id);
		when(v.getLatitude()).thenReturn(lat);
		when(v.getLongitude()).thenReturn(lon);
		return v;
	}

	/** MoveTime 빌더로 생성 후 id를 리플렉션으로 주입
	 *  내부 from/to 목의 getId()는 toResponseDTO 경로에 따라 호출되지 않을 수 있으므로 lenient 처리 */
	private MoveTime moveTime(Long id, Long fromId, Long toId, int minutes, int distance, String polyline) {
		Vlock from = mock(Vlock.class);
		lenient().when(from.getId()).thenReturn(fromId);
		Vlock to = mock(Vlock.class);
		lenient().when(to.getId()).thenReturn(toId);

		MoveTime mt = MoveTime.builder()
			.fromVlock(from)
			.toVlock(to)
			.moveMinutes(minutes)
			.transportType(WALK)
			.distanceMeter(distance)
			.polyline(polyline)
			.build();
		ReflectionTestUtils.setField(mt, "id", id);
		return mt;
	}
}
