package org.umc.travlocksserver.domain.notification.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

// ✨SSE 연결이 끊기지 않도록 일정 주기로 모든 연결된 클라이언트에게 heartbeat(ping)을 보내는 스케줄러
@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

	private static final String HEARTBEAT_DATA = "ping";

	private final SseEmitterRepository emitterRepository;

	@Scheduled(fixedDelayString = "${sse.heartbeat-ms}")
	public void heartbeat() {
		// 현재 연결된 모든 emitter, 사용자 조회
		for (var memberEntry : emitterRepository.getAll().entrySet()) {
			Long memberId = memberEntry.getKey();

			// 한 사용자의 emitter 목록 조회 (한 사용자는 여러 탭, 디바이스 등 연결 가능)
			Map<String, SseEmitter> memberEmitters = memberEntry.getValue();

			for (var emitterEntry : memberEmitters.entrySet()) {
				String emitterId = emitterEntry.getKey();
				SseEmitter emitter = emitterEntry.getValue(); // SSE 연결 emitter 객체

				try {
					// SSE 이벤트 전송
					emitter.send(
						SseEmitter.event()
							.name(SseEventNames.HEARTBEAT)
							.data(HEARTBEAT_DATA));
				} catch (IOException e) {
					emitterRepository.remove(memberId, emitterId); // emitter 제거
				}
			}
		}
	}
}
