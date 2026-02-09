package org.umc.travlocksserver.domain.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.umc.travlocksserver.domain.notification.service.command.NotificationCommandService;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventHandler {

    private final NotificationCommandService notificationCommandService;

    /**
     * 이벤트 발생 커밋 시 알림 생성 및 미읽음 신호 전송
     * */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTemplateActivity(TemplateActivityEvent event) {
        try {
            notificationCommandService.createNotification(
                    event.ownerId(),
                    event.actorId(),
                    event.templateId(),
                    event.type()
            );

            notificationCommandService.signalHasUnread(event.ownerId(), true);
        } catch (Exception e) {
            log.warn("알림 생성 중 오류가 발생했습니다. event={}", event, e);
        }

    }
}
