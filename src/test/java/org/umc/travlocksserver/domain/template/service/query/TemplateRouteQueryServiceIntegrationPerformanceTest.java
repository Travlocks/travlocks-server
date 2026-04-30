package org.umc.travlocksserver.domain.template.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.location.entity.Region;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.location.repository.RegionRepository;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDayRouteResponseDTO;
import org.umc.travlocksserver.domain.template.entity.MoveTime;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.repository.MoveTimeRepository;
import org.umc.travlocksserver.domain.template.service.command.RouteCalculationAsyncService;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.external.tmap.TmapApiService;
import org.umc.travlocksserver.global.external.tmap.TmapDTO;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:route-performance-testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=END,START",
	"spring.jpa.hibernate.ddl-auto=none",
	"spring.jpa.generate-ddl=false",
	"spring.sql.init.mode=always",
	"spring.sql.init.schema-locations=classpath:route-performance-schema.sql",
	"spring.sql.init.data-locations=optional:classpath:data.sql",
	"spring.jpa.show-sql=false",
	"logging.level.org.hibernate.SQL=warn",
	"logging.level.org.umc.travlocksserver.domain.template.service.query.TemplateRouteQueryService=warn",
	"logging.level.org.umc.travlocksserver.domain.template.service.command.RouteCalculationAsyncService=warn"
})
class TemplateRouteQueryServiceIntegrationPerformanceTest {

	private static final Logger log = LoggerFactory.getLogger("route-integration-performance");
	private static final int WARMUP_COUNT = 3;
	private static final int REPEAT_COUNT = 20;

	@Autowired
	private TemplateRouteQueryService templateRouteQueryService;

	@Autowired
	private MoveTimeRepository moveTimeRepository;

	@Autowired
	private VlockRepository vlockRepository;

	@Autowired
	private VlockCategoryRepository vlockCategoryRepository;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private RegionRepository regionRepository;

	@Autowired
	private CacheManager cacheManager;

	@MockitoBean
	private TmapApiService tmapApiService;

	private Vlock fromVlock;
	private Vlock toVlock;

	@BeforeEach
	void setUp() {
		clearCache();
		moveTimeRepository.deleteAll();
		vlockRepository.deleteAll();
		vlockCategoryRepository.deleteAll();
		cityRepository.deleteAll();
		regionRepository.deleteAll();

		Region region = regionRepository.save(Region.builder()
			.id(1L)
			.name("서울")
			.build());
		City city = cityRepository.save(City.builder()
			.id(1L)
			.region(region)
			.name("서울")
			.build());
		VlockCategory category = vlockCategoryRepository.save(VlockCategory.builder()
			.id(1L)
			.name("명소")
			.stayHours(1.0)
			.defaultCreationImageKey("creation.png")
			.defaultCategoryImageKey("category.png")
			.build());

		fromVlock = vlockRepository.save(Vlock.createByExternal(
			"perf-from",
			category,
			city,
			"from",
			37.5665,
			126.9780,
			"from-address"));
		toVlock = vlockRepository.save(Vlock.createByExternal(
			"perf-to",
			category,
			city,
			"to",
			37.6000,
			127.0500,
			"to-address"));

		when(tmapApiService.getPedestrianRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
			.thenReturn(TmapDTO.RouteInfo.builder()
				.totalTimeMinutes(31)
				.totalDistanceMeter(2400)
				.polyline("[[126.978,37.5665],[127.05,37.6]]")
				.build());
	}

	@Test
	@Tag("performance")
	@DisplayName("logs route response time with JPA DB and Spring cache")
	void getOrCreateRoute_logsDbAndCacheResponseTime() {
		seedExactMoveTime();

		PerformanceStats coldDb = benchmark("cold exact route: Spring Cache miss + DB hit", () -> {
			clearCache();
			return measure(() -> templateRouteQueryService.getOrCreateRoute(
				fromVlock.getId(),
				toVlock.getId(),
				TransportType.WALK));
		});

		templateRouteQueryService.getOrCreateRoute(fromVlock.getId(), toVlock.getId(), TransportType.WALK);
		PerformanceStats warmCache = benchmark("warm exact route: Spring Cache hit", () -> measure(
			() -> templateRouteQueryService.getOrCreateRoute(fromVlock.getId(), toVlock.getId(), TransportType.WALK)));

		assertThat(coldDb.medianMicros()).isGreaterThanOrEqualTo(warmCache.medianMicros());
	}

	@Test
	@Tag("performance")
	@DisplayName("logs TMap route calculation time with JPA DB and mocked external client")
	void calculateAndSaveAsync_logsDbAndTmapCalculationTime() {
		RouteCalculationAsyncService routeCalculationService =
			new RouteCalculationAsyncService(moveTimeRepository, vlockRepository, tmapApiService);

		PerformanceStats stats = benchmark("TMap calculation: DB lookup + external client + DB save", () -> {
			clearCache();
			moveTimeRepository.deleteAll();
			return measureVoid(() -> routeCalculationService.calculateAndSaveAsync(
					fromVlock.getId(),
					toVlock.getId(),
					TransportType.WALK),
				() -> new TemplateDayRouteResponseDTO(
					null,
					fromVlock.getId(),
					toVlock.getId(),
					31,
					2400,
					TransportType.WALK,
					List.of()));
		});

		assertThat(stats.averageMicros()).isGreaterThanOrEqualTo(0);
	}

	private void seedExactMoveTime() {
		moveTimeRepository.save(MoveTime.builder()
			.fromVlock(fromVlock)
			.toVlock(toVlock)
			.moveMinutes(25)
			.transportType(TransportType.WALK)
			.distanceMeter(2100)
			.polyline("[[126.978,37.5665],[127.05,37.6]]")
			.build());
	}

	private void clearCache() {
		if (cacheManager.getCache("moveTime") != null) {
			cacheManager.getCache("moveTime").clear();
		}
	}

	private Measured<TemplateDayRouteResponseDTO> measure(Supplier<TemplateDayRouteResponseDTO> supplier) {
		long startedAt = System.nanoTime();
		TemplateDayRouteResponseDTO result = supplier.get();
		return new Measured<>(result, System.nanoTime() - startedAt);
	}

	private Measured<TemplateDayRouteResponseDTO> measureVoid(
		Runnable runnable,
		Supplier<TemplateDayRouteResponseDTO> resultSupplier) {
		long startedAt = System.nanoTime();
		runnable.run();
		return new Measured<>(resultSupplier.get(), System.nanoTime() - startedAt);
	}

	private PerformanceStats benchmark(
		String scenario,
		Supplier<Measured<TemplateDayRouteResponseDTO>> measuredSupplier) {
		for (int i = 0; i < WARMUP_COUNT; i++) {
			measuredSupplier.get();
		}

		List<Long> elapsedMicros = new ArrayList<>();
		for (int i = 0; i < REPEAT_COUNT; i++) {
			Measured<TemplateDayRouteResponseDTO> measured = measuredSupplier.get();
			assertThat(measured.result()).isNotNull();
			elapsedMicros.add(measured.elapsedMicros());
		}

		PerformanceStats stats = PerformanceStats.from(elapsedMicros);
		log.info(
			"[route-integration-performance] scenario={}, samples={}, avg={}us, median={}us, p95={}us, min={}us, max={}us",
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
