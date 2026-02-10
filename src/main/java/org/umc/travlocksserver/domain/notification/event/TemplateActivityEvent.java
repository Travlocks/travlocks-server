package org.umc.travlocksserver.domain.notification.event;

import org.umc.travlocksserver.domain.notification.enums.NotificationType;

public record TemplateActivityEvent(
        Long ownerId,
        Long actorId,
        String actorNickname,
        Long templateId,
        NotificationType type
) {
}
