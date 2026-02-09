package org.umc.travlocksserver.domain.notification.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.service.query.MemberQueryService;
import org.umc.travlocksserver.domain.notification.dto.sse.NotificationSsePayloadDTO;
import org.umc.travlocksserver.domain.notification.entity.Notification;
import org.umc.travlocksserver.domain.notification.enums.NotificationType;
import org.umc.travlocksserver.domain.notification.repository.NotificationRepository;
import org.umc.travlocksserver.domain.notification.sse.SseEmitterRepository;
import org.umc.travlocksserver.domain.notification.sse.SseEventNames;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    @Value("${sse.timeout-ms}")
    private long SSE_TIMEOUT_MS;

    private final SseEmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final MemberQueryService memberQueryService;

    /**
     * 클라이언트의 SSE 연결 생성 및 등록
     * - 로그인한 사용자가 실시간 알림을 수신할 수 있도록 서버와 클라이언트 간 SSE 스트림 연결 생성
    * */
    public SseEmitter subscribe(Long memberId) {
        String emitterId = memberId + "-" + UUID.randomUUID();  // 한 유저는 여러 연결을 가질 수 있음 emitterId로 관리
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);  // SSE 연결 생성

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

    @Transactional
    public void createNotification(Long receiverId, Long actorId, Long templateId, NotificationType type) {
        Member member = memberQueryService.getById(actorId);
        notificationRepository.save(
                Notification.create(
                        receiverId,
                        actorId,
                        member.getNickname(),
                        templateId,
                        type)
        );
    }

    /**
     * 사용자에게 읽지 않은 알림이 있음(SSE 이벤트)을 실시간으로 푸시
     * */
    public void signalHasUnread(Long receiverId) {
        Map<String, SseEmitter> emitters = emitterRepository.getEmitters(receiverId);
        if (emitters.isEmpty())
            return;

        NotificationSsePayloadDTO payload = new NotificationSsePayloadDTO(true);

        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            try {
                entry.getValue().send(
                        SseEmitter.event()
                                .name(SseEventNames.NOTIFICATION)
                                .data(payload)
                );
            } catch (IOException e) {  // SSE 연결이 끊겼는데 보내려고 할 떄
                emitterRepository.remove(receiverId, entry.getKey());  // 연결 정리
            }
        }
    }
}
