package org.umc.travlocksserver.domain.template.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.umc.travlocksserver.domain.template.entity.MoveTime;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.repository.MoveTimeRepository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.external.tmap.TmapApiService;
import org.umc.travlocksserver.global.external.tmap.TmapDTO;

@ExtendWith(MockitoExtension.class)
class RouteCalculationAsyncServiceTest {

	@Mock
	private MoveTimeRepository moveTimeRepository;

	@Mock
	private VlockRepository vlockRepository;

	@Mock
	private TmapApiService tmapApiService;

	@Captor
	private ArgumentCaptor<MoveTime> moveTimeCaptor;

	@Test
	// DB에 이미 경로가 저장되어 있으면 TMap API 호출과 추가 저장을 생략한다.
	@DisplayName("skips external API call when DB route already exists")
	void calculateAndSaveAsync_skipsExternalApiWhenRouteAlreadyExists() {
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.6000, 127.0500);
		MoveTime existing = MoveTime.builder()
			.fromVlock(from)
			.toVlock(to)
			.moveMinutes(25)
			.distanceMeter(2100)
			.transportType(TransportType.WALK)
			.polyline("[[126.978,37.5665],[127.05,37.6]]")
			.build();

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.of(existing));

		RouteCalculationAsyncService service =
			new RouteCalculationAsyncService(moveTimeRepository, vlockRepository, tmapApiService);

		service.calculateAndSaveAsync(1L, 2L, TransportType.WALK);

		verify(vlockRepository, never()).findById(any());
		verify(tmapApiService, never()).getPedestrianRoute(any(), any(), any(), any());
		verify(moveTimeRepository, never()).save(any(MoveTime.class));
	}

	@Test
	// TMap 응답의 이동시간, 거리, polyline이 변경 없이 MoveTime에 저장되는지 검증한다.
	@DisplayName("saves TMap result values without changing them")
	void calculateAndSaveAsync_savesTmapResult() {
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.6000, 127.0500);
		TmapDTO.RouteInfo routeInfo = TmapDTO.RouteInfo.builder()
			.totalTimeMinutes(31)
			.totalDistanceMeter(2400)
			.polyline("[[126.978,37.5665],[127.05,37.6]]")
			.build();

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(vlockRepository.findById(1L)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(2L)).thenReturn(Optional.of(to));
		when(tmapApiService.getPedestrianRoute(126.9780, 37.5665, 127.0500, 37.6000))
			.thenReturn(routeInfo);

		RouteCalculationAsyncService service =
			new RouteCalculationAsyncService(moveTimeRepository, vlockRepository, tmapApiService);

		service.calculateAndSaveAsync(1L, 2L, TransportType.WALK);

		verify(moveTimeRepository).save(moveTimeCaptor.capture());
		MoveTime saved = moveTimeCaptor.getValue();
		assertThat(saved.getFromVlock()).isSameAs(from);
		assertThat(saved.getToVlock()).isSameAs(to);
		assertThat(saved.getTransportType()).isEqualTo(TransportType.WALK);
		assertThat(saved.getMoveMinutes()).isEqualTo(31);
		assertThat(saved.getDistanceMeter()).isEqualTo(2400);
		assertThat(saved.getPolyline()).isEqualTo("[[126.978,37.5665],[127.05,37.6]]");
	}

	@Test
	// 동일 route key가 이미 계산 중이면 두 번째 요청은 TMap API를 다시 호출하지 않는다.
	@DisplayName("limits duplicate in-progress route API calls to one")
	void calculateAndSaveAsync_skipsDuplicateInProgressRouteKey() throws Exception {
		Vlock from = vlock(1L, 37.5665, 126.9780);
		Vlock to = vlock(2L, 37.6000, 127.0500);
		TmapDTO.RouteInfo routeInfo = TmapDTO.RouteInfo.builder()
			.totalTimeMinutes(31)
			.totalDistanceMeter(2400)
			.polyline("[[126.978,37.5665],[127.05,37.6]]")
			.build();
		CountDownLatch tmapEntered = new CountDownLatch(1);
		CountDownLatch releaseTmap = new CountDownLatch(1);

		when(moveTimeRepository.findByFromVlockIdAndToVlockIdAndTransportType(1L, 2L, TransportType.WALK))
			.thenReturn(Optional.empty());
		when(vlockRepository.findById(1L)).thenReturn(Optional.of(from));
		when(vlockRepository.findById(2L)).thenReturn(Optional.of(to));
		when(tmapApiService.getPedestrianRoute(126.9780, 37.5665, 127.0500, 37.6000))
			.thenAnswer(invocation -> {
				tmapEntered.countDown();
				assertThat(releaseTmap.await(2, TimeUnit.SECONDS)).isTrue();
				return routeInfo;
			});

		RouteCalculationAsyncService service =
			new RouteCalculationAsyncService(moveTimeRepository, vlockRepository, tmapApiService);

		CompletableFuture<Void> firstCall = CompletableFuture.runAsync(
			() -> service.calculateAndSaveAsync(1L, 2L, TransportType.WALK));
		assertThat(tmapEntered.await(2, TimeUnit.SECONDS)).isTrue();

		service.calculateAndSaveAsync(1L, 2L, TransportType.WALK);

		releaseTmap.countDown();
		firstCall.get(2, TimeUnit.SECONDS);

		verify(tmapApiService, timeout(1000).times(1))
			.getPedestrianRoute(126.9780, 37.5665, 127.0500, 37.6000);
		verify(moveTimeRepository).save(any(MoveTime.class));
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
}
