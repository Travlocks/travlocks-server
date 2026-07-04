package org.umc.travlocksserver.domain.notification.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.service.command.MemberProfileUpdateService;
import org.umc.travlocksserver.domain.member.service.query.MemberQueryService;
import org.umc.travlocksserver.domain.notification.dto.response.NotificationAllResponseDTO;
import org.umc.travlocksserver.domain.notification.dto.sse.NotificationSsePayloadDTO;
import org.umc.travlocksserver.domain.notification.entity.Notification;
import org.umc.travlocksserver.domain.notification.enums.NotificationType;
import org.umc.travlocksserver.domain.notification.repository.NotificationRepository;
import org.umc.travlocksserver.domain.notification.sse.SseEmitterRepository;
import org.umc.travlocksserver.domain.notification.sse.SseEventNames;
import org.umc.travlocksserver.global.jwt.JwtTokenProvider;
import org.umc.travlocksserver.global.util.TimeAgoFormatter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

	@Value("${sse.timeout-ms}")
	private long SSE_TIMEOUT_MS;

	@Value("${cookie.sse-token.token-ttl-ms}")
	private long SSE_TOKEN_TTL_MS;

	private final SseEmitterRepository emitterRepository;
	private final NotificationRepository notificationRepository;
	private final MemberQueryService memberQueryService;
	private final MemberProfileUpdateService memberProfileUpdateService;
	private final JwtTokenProvider jwtTokenProvider;
	private final TimeAgoFormatter timeAgoFormatter;

	/**
	 * 클라이언트의 SSE 연결 생성 및 등록
	 * - 로그인한 사용자가 실시간 알림을 수신할 수 있도록 서버와 클라이언트 간 SSE 스트림 연결 생성
	* */
	public SseEmitter subscribe(Long memberId) {
		String emitterId = memberId + "-" + UUID.randomUUID(); // 한 유저는 여러 연결을 가질 수 있음 emitterId로 관리
		SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS); // SSE 연결 생성

		emitterRepository.add(memberId, emitterId, emitter);

		emitter.onCompletion(() -> emitterRepository.remove(memberId, emitterId));
		emitter.onTimeout(() -> emitterRepository.remove(memberId, emitterId));
		emitter.onError(e -> emitterRepository.remove(memberId, emitterId));

		// 연결 직후 connected 이벤트 전송 (프론트 연결 확인, 프록시 연결 유지를 위함)
		try {
			emitter.send(
				SseEmitter.event()
					.name(SseEventNames.CONNECTED)
					.data("ok"));
		} catch (IOException e) {
			try {
				emitter.complete();
			} catch (Exception ignored) {}
		}

		return emitter;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Notification createNotification(Long receiverId, Long actorId, Long templateId, NotificationType type) {
		Member actor = memberQueryService.getById(actorId);
		memberProfileUpdateService.increaseNotificationCount(receiverId);
		return notificationRepository.save(
			Notification.create(
				receiverId,
				actorId,
				actor.getNickname(),
				templateId,
				type)
		);
	}

	/**
	 * 사용자에게 읽지 않은 알림이 있음(SSE 이벤트)을 실시간으로 푸시
	 * */
	public void signalHasUnread(Long receiverId, boolean hasUnread) {
		Map<String, SseEmitter> emitters = emitterRepository.getEmitters(receiverId);
		if (emitters.isEmpty())
			return;

		NotificationSsePayloadDTO payload = new NotificationSsePayloadDTO(hasUnread);

		for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
			String emitterId = entry.getKey();
			SseEmitter emitter = entry.getValue();
			try {
				emitter.send(
					SseEmitter.event()
						.name(SseEventNames.UNREAD)
						.data(payload));
			} catch (IOException e) { // SSE 연결이 끊겼는데 보내려고 할 떄
				emitterRepository.remove(receiverId, emitterId); // 연결 정리
			}
		}
	}

	/**
	 * 사용자에게 들어온 새로운 알림(SSE 이벤트)을 실시간으로 푸시
	 * */
	public void pushNotificationCreated(Notification notification) {
		Map<String, SseEmitter> emitters = emitterRepository.getEmitters(notification.getReceiverId());

		if (emitters.isEmpty()) {
			return;
		}

		NotificationAllResponseDTO.NotificationDTO payload = NotificationAllResponseDTO.NotificationDTO
			.from(notification, timeAgoFormatter.format(notification.getCreatedAt()));

		for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
			String emitterId = entry.getKey();
			SseEmitter emitter = entry.getValue();

			try {
				emitter.send(
					SseEmitter.event()
						.name(SseEventNames.NOTIFICATION)
						.data(payload));
			} catch (IOException e) {
				emitterRepository.remove(notification.getReceiverId(), emitterId);
			}
		}
	}

	public String generateSseToken(Long memberId) {
		long ttlSeconds = SSE_TOKEN_TTL_MS / 1000L;
		return jwtTokenProvider.generateSseToken(memberId, ttlSeconds);
	}

	@Transactional
	public void deleteAllNotification(Long memberId) {
		notificationRepository.deleteAllByReceiverId(memberId);
		memberQueryService.getById(memberId).clearNotificationCount();
		signalHasUnread(memberId, false);
	}
}
