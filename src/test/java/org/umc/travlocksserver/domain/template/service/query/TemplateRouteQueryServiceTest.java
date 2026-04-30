package org.umc.travlocksserver.domain.template.service.query;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import ch.qos.logback.classic.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDayRouteResponseDTO;
import org.umc.travlocksserver.domain.template.entity.MoveTime;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.repository.MoveTimeRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.template.service.command.RouteCalculationAsyncService;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.util.PolylineUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TemplateRouteQueryServiceTest {

	private static final Logger log = LoggerFactory.getLogger("route-performance");
	private static final Duration FAST_RESPONSE_TIMEOUT = Duration.ofMillis(500);
	private static final int PERFORMANCE_WARMUP_COUNT = 5;
	private static final int PERFORMANCE_REPEAT_COUNT = 50;

	@Mock
	private MoveTimeRepository moveTimeRepository;

	@Mock
	private TemplateDayRepository templateDayRepository;

	@Mock
	private TemplateVlockRepository templateVlockRepository;

	@Mock
	private VlockRepository vlockRepository;

	@Mock
	private RouteCalculationAsyncService routeCalculationAsyncService;

	@Captor
	private ArgumentCaptor<MoveTime> moveTimeCaptor;

	private TemplateRouteQueryService templateRouteQueryService;

	@BeforeEach
	void setUp() {
		templateRouteQueryService = new TemplateRouteQueryService(
			moveTimeRepository,
			templateDayRepository,
			templateVlockRepository,
			vlockRepository,
			routeCalculationAsyncService,
			new PolylineUtil(new ObjectMapper()));
	}

	@Test
	// 정확히 일치하는 DB 경로가 있으면 Vlock 조회, 저장, 비동기 계산 없이 기존 결과를 그대로 재사용한다.
	@DisplayName("reuses exact DB route without recalculation")
	void getOrCreateRoute_reusesExactDbRoute() {
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.5700, 126.9820);
		MoveTime cached = moveTime(10L, from, to, 7, 520, "[[126.978,37.5665],[126.982,37.57]]");

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.of(cached));

		TemplateDayRouteResponseDTO result = assertTimeout(
			FAST_RESPONSE_TIMEOUT,
			() -> templateRouteQueryService.getOrCreateRoute(1L, 2L, TransportType.WALK));

		assertThat(result.moveTimeId()).isEqualTo(10L);
		assertThat(result.fromVlockId()).isEqualTo(1L);
		assertThat(result.toVlockId()).isEqualTo(2L);
		assertThat(result.moveMinutes()).isEqualTo(7);
		assertThat(result.distanceMeter()).isEqualTo(520);
		assertThat(result.polyline()).containsExactly(
			List.of(126.978, 37.5665),
			List.of(126.982, 37.57));

		verify(vlockRepository, never()).findById(any());
		verify(moveTimeRepository, never()).findTopReusableNearbyRoute(any(), any(), any(), any(), any(Double.class),
			any(Double.class), any());
		verify(moveTimeRepository, never()).save(any(MoveTime.class));
		verify(routeCalculationAsyncService, never()).calculateAndSaveAsync(any(), any(), any());
	}

	@Test
	// 근처 DB 경로가 있으면 이동시간, 거리, polyline을 복사해 저장하고 외부 경로 계산은 호출하지 않는다.
	@DisplayName("reuses nearby DB route without external route calculation")
	void getOrCreateRoute_reusesNearbyRoute() {
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.5750, 126.9900);
		Vlock reusableFrom = vlock(3L, 37.5666, 126.9781);
		Vlock reusableTo = vlock(4L, 37.5751, 126.9901);
		MoveTime reusable = moveTime(20L, reusableFrom, reusableTo, 16, 1200,
			"[[126.9781,37.5666],[126.9901,37.5751]]");

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(vlockRepository.findById(1L)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(2L)).thenReturn(Optional.of(to));
		when(moveTimeRepository.findTopReusableNearbyRoute(
			37.5665, 126.9780, 37.5750, 126.9900, 0.001, 0.001, TransportType.WALK))
			.thenReturn(Optional.of(reusable));
		when(moveTimeRepository.save(any(MoveTime.class))).thenAnswer(invocation -> {
			MoveTime saved = invocation.getArgument(0);
			ReflectionTestUtils.setField(saved, "id", 30L);
			return saved;
		});

		TemplateDayRouteResponseDTO result = assertTimeout(
			FAST_RESPONSE_TIMEOUT,
			() -> templateRouteQueryService.getOrCreateRoute(1L, 2L, TransportType.WALK));

		assertThat(result.moveTimeId()).isEqualTo(30L);
		assertThat(result.fromVlockId()).isEqualTo(1L);
		assertThat(result.toVlockId()).isEqualTo(2L);
		assertThat(result.moveMinutes()).isEqualTo(16);
		assertThat(result.distanceMeter()).isEqualTo(1200);
		assertThat(result.polyline()).containsExactly(
			List.of(126.9781, 37.5666),
			List.of(126.9901, 37.5751));

		verify(moveTimeRepository).save(moveTimeCaptor.capture());
		MoveTime saved = moveTimeCaptor.getValue();
		assertThat(saved.getFromVlock()).isSameAs(from);
		assertThat(saved.getToVlock()).isSameAs(to);
		assertThat(saved.getMoveMinutes()).isEqualTo(reusable.getMoveMinutes());
		assertThat(saved.getDistanceMeter()).isEqualTo(reusable.getDistanceMeter());
		assertThat(saved.getPolyline()).isEqualTo(reusable.getPolyline());
		verify(routeCalculationAsyncService, never()).calculateAndSaveAsync(any(), any(), any());
	}

	@Test
	// 짧은 도보 거리는 외부 API/비동기 계산 없이 자체 계산한 결과를 저장한다.
	@DisplayName("calculates short walking distance locally")
	void getOrCreateRoute_calculatesShortDistanceLocally() {
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.5670, 126.9785);

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(vlockRepository.findById(1L)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(2L)).thenReturn(Optional.of(to));
		when(moveTimeRepository.save(any(MoveTime.class))).thenAnswer(invocation -> {
			MoveTime saved = invocation.getArgument(0);
			ReflectionTestUtils.setField(saved, "id", 40L);
			return saved;
		});

		TemplateDayRouteResponseDTO result = assertTimeout(
			FAST_RESPONSE_TIMEOUT,
			() -> templateRouteQueryService.getOrCreateRoute(1L, 2L, TransportType.WALK));

		assertThat(result.moveTimeId()).isEqualTo(40L);
		assertThat(result.fromVlockId()).isEqualTo(1L);
		assertThat(result.toVlockId()).isEqualTo(2L);
		assertThat(result.transportType()).isEqualTo(TransportType.WALK);
		assertThat(result.distanceMeter()).isPositive();
		assertThat(result.moveMinutes()).isPositive();
		assertThat(result.polyline()).containsExactly(
			List.of(126.9780, 37.5665),
			List.of(126.9785, 37.5670));

		verify(moveTimeRepository, never()).findTopReusableNearbyRoute(any(), any(), any(), any(), any(Double.class),
			any(Double.class), any());
		verify(routeCalculationAsyncService, never()).calculateAndSaveAsync(any(), any(), any());
	}

	@Test
	// 정확 DB 캐시와 근처 경로가 모두 없으면 fallback을 반환하고 비동기 계산만 트리거한다.
	@DisplayName("returns fallback and triggers async calculation on route cache miss")
	void getOrCreateRoute_triggersAsyncCalculationOnMiss() {
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.6000, 127.0500);

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(vlockRepository.findById(1L)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(2L)).thenReturn(Optional.of(to));
		when(moveTimeRepository.findTopReusableNearbyRoute(
			37.5665, 126.9780, 37.6000, 127.0500, 0.001, 0.001, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(moveTimeRepository.findTopReusableNearbyRouteReverse(
			37.5665, 126.9780, 37.6000, 127.0500, 0.001, 0.001, TransportType.WALK))
			.thenReturn(Optional.empty());

		TemplateDayRouteResponseDTO result = assertTimeout(
			FAST_RESPONSE_TIMEOUT,
			() -> templateRouteQueryService.getOrCreateRoute(1L, 2L, TransportType.WALK));

		assertThat(result.moveTimeId()).isNull();
		assertThat(result.moveMinutes()).isZero();
		assertThat(result.distanceMeter()).isZero();
		assertThat(result.polyline()).isEmpty();

		verify(routeCalculationAsyncService).calculateAndSaveAsync(1L, 2L, TransportType.WALK);
		verify(moveTimeRepository, never()).save(any(MoveTime.class));
	}

	@Test
	@Tag("performance")
	@DisplayName("logs getOrCreateRoute response time by optimized path")
	void getOrCreateRoute_logsResponseTimeByOptimizedPath() {
		PerformanceStats exactDbRoute = benchmarkWithRouteServiceLogMuted("exact DB route", this::measureExactDbRoute);
		PerformanceStats nearbyDbRoute = benchmarkWithRouteServiceLogMuted(
			"nearby DB route reuse",
			this::measureNearbyDbRoute);
		PerformanceStats shortDistanceRoute = benchmarkWithRouteServiceLogMuted(
			"short distance local calculation",
			this::measureShortDistanceRoute);
		PerformanceStats fallbackAsyncTriggerRoute = benchmarkWithRouteServiceLogMuted(
			"fallback async trigger",
			this::measureFallbackAsyncTriggerRoute);

		assertThat(exactDbRoute.averageMicros()).isGreaterThanOrEqualTo(0);
		assertThat(nearbyDbRoute.averageMicros()).isGreaterThanOrEqualTo(0);
		assertThat(shortDistanceRoute.averageMicros()).isGreaterThanOrEqualTo(0);
		assertThat(fallbackAsyncTriggerRoute.averageMicros()).isGreaterThanOrEqualTo(0);
	}

	private Vlock vlock(Long id, Double latitude, Double longitude) {
		Vlock vlock = Vlock.createByExternal(
			"place-" + id,
			null,
			null,
			"vlock-" + id,
			latitude,
			longitude,
			"address-" + id);
		ReflectionTestUtils.setField(vlock, "id", id);
		return vlock;
	}

	private MoveTime moveTime(
		Long id,
		Vlock from,
		Vlock to,
		Integer moveMinutes,
		Integer distanceMeter,
		String polyline) {
		MoveTime moveTime = MoveTime.builder()
			.fromVlock(from)
			.toVlock(to)
			.moveMinutes(moveMinutes)
			.distanceMeter(distanceMeter)
			.transportType(TransportType.WALK)
			.polyline(polyline)
			.build();
		ReflectionTestUtils.setField(moveTime, "id", id);
		return moveTime;
	}

	private Measured<TemplateDayRouteResponseDTO> measureExactDbRoute() {
		reset(moveTimeRepository, vlockRepository, routeCalculationAsyncService);
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.5700, 126.9820);
		MoveTime cached = moveTime(10L, from, to, 7, 520, "[[126.978,37.5665],[126.982,37.57]]");

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.of(cached));

		return measure(() -> templateRouteQueryService.getOrCreateRoute(1L, 2L, TransportType.WALK));
	}

	private Measured<TemplateDayRouteResponseDTO> measureNearbyDbRoute() {
		reset(moveTimeRepository, vlockRepository, routeCalculationAsyncService);
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.5750, 126.9900);
		Vlock reusableFrom = vlock(3L, 37.5666, 126.9781);
		Vlock reusableTo = vlock(4L, 37.5751, 126.9901);
		MoveTime reusable = moveTime(20L, reusableFrom, reusableTo, 16, 1200,
			"[[126.9781,37.5666],[126.9901,37.5751]]");

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(vlockRepository.findById(1L)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(2L)).thenReturn(Optional.of(to));
		when(moveTimeRepository.findTopReusableNearbyRoute(
			37.5665, 126.9780, 37.5750, 126.9900, 0.001, 0.001, TransportType.WALK))
			.thenReturn(Optional.of(reusable));
		when(moveTimeRepository.save(any(MoveTime.class))).thenAnswer(invocation -> {
			MoveTime saved = invocation.getArgument(0);
			ReflectionTestUtils.setField(saved, "id", 30L);
			return saved;
		});

		return measure(() -> templateRouteQueryService.getOrCreateRoute(1L, 2L, TransportType.WALK));
	}

	private Measured<TemplateDayRouteResponseDTO> measureShortDistanceRoute() {
		reset(moveTimeRepository, vlockRepository, routeCalculationAsyncService);
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.5670, 126.9785);

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(vlockRepository.findById(1L)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(2L)).thenReturn(Optional.of(to));
		when(moveTimeRepository.save(any(MoveTime.class))).thenAnswer(invocation -> {
			MoveTime saved = invocation.getArgument(0);
			ReflectionTestUtils.setField(saved, "id", 40L);
			return saved;
		});

		return measure(() -> templateRouteQueryService.getOrCreateRoute(1L, 2L, TransportType.WALK));
	}

	private Measured<TemplateDayRouteResponseDTO> measureFallbackAsyncTriggerRoute() {
		reset(moveTimeRepository, vlockRepository, routeCalculationAsyncService);
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.6000, 127.0500);

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(vlockRepository.findById(1L)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(2L)).thenReturn(Optional.of(to));
		when(moveTimeRepository.findTopReusableNearbyRoute(
			37.5665, 126.9780, 37.6000, 127.0500, 0.001, 0.001, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(moveTimeRepository.findTopReusableNearbyRouteReverse(
			37.5665, 126.9780, 37.6000, 127.0500, 0.001, 0.001, TransportType.WALK))
			.thenReturn(Optional.empty());

		return measure(() -> templateRouteQueryService.getOrCreateRoute(1L, 2L, TransportType.WALK));
	}

	private <T> Measured<T> measure(Supplier<T> supplier) {
		long startedAt = System.nanoTime();
		T result = supplier.get();
		return new Measured<>(result, System.nanoTime() - startedAt);
	}

	private PerformanceStats benchmarkWithRouteServiceLogMuted(
		String scenario,
		Supplier<Measured<TemplateDayRouteResponseDTO>> measuredSupplier) {
		ch.qos.logback.classic.Logger routeLogger =
			(ch.qos.logback.classic.Logger)LoggerFactory.getLogger(TemplateRouteQueryService.class);
		Level previousLevel = routeLogger.getLevel();
		routeLogger.setLevel(Level.WARN);
		try {
			return benchmark(scenario, measuredSupplier);
		} finally {
			routeLogger.setLevel(previousLevel);
		}
	}

	private PerformanceStats benchmark(String scenario, Supplier<Measured<TemplateDayRouteResponseDTO>> measuredSupplier) {
		for (int i = 0; i < PERFORMANCE_WARMUP_COUNT; i++) {
			measuredSupplier.get();
		}

		List<Long> elapsedMicros = new ArrayList<>();
		for (int i = 0; i < PERFORMANCE_REPEAT_COUNT; i++) {
			Measured<TemplateDayRouteResponseDTO> measured = measuredSupplier.get();
			assertThat(measured.result()).isNotNull();
			elapsedMicros.add(measured.elapsedMicros());
		}

		PerformanceStats stats = PerformanceStats.from(elapsedMicros);
		log.info(
			"[route-performance] scenario={}, samples={}, avg={}us, median={}us, p95={}us, min={}us, max={}us",
			scenario,
			elapsedMicros.size(),
			stats.averageMicros(),
			stats.medianMicros(),
			stats.p95Micros(),
			stats.minMicros(),
			stats.maxMicros());
		return stats;
	}

	private record Measured<T>(T result, long elapsedNanos) {
		long elapsedMicros() {
			return elapsedNanos / 1_000;
		}
	}

	private record PerformanceStats(
		long averageMicros,
		long medianMicros,
		long p95Micros,
		long minMicros,
		long maxMicros) {

		private static PerformanceStats from(List<Long> values) {
			List<Long> sorted = new ArrayList<>(values);
			Collections.sort(sorted);
			long sum = 0;
			for (Long value : sorted) {
				sum += value;
			}
			return new PerformanceStats(
				sum / sorted.size(),
				percentile(sorted, 0.50),
				percentile(sorted, 0.95),
				sorted.get(0),
				sorted.get(sorted.size() - 1));
		}

		private static long percentile(List<Long> sorted, double percentile) {
			int index = (int)Math.ceil(sorted.size() * percentile) - 1;
			return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
		}
	}
}
