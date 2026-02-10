package org.umc.travlocksserver.domain.notification.dto.response;

import org.umc.travlocksserver.domain.notification.entity.Notification;
import org.umc.travlocksserver.domain.notification.enums.NotificationType;
import org.umc.travlocksserver.global.util.TimeAgoFormatter;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationAllResponseDTO(
        int notificationCount,
        boolean hasNext,
        String nextCursor,
        List<NotificationDTO> notifications
) {
    public record NotificationDTO(
            Long notificationId,
            Long actorId,
            String actorNickname,
            Long templateId,
            NotificationType type,
            LocalDateTime createdAt,
            String timeAgo
    ) {
        public static NotificationDTO from(Notification noti, String timeAgo) {
            return new NotificationDTO(
                    noti.getId(),
                    noti.getActorId(),
                    noti.getActorNicknameSnapshot(),
                    noti.getTemplateId(),
                    noti.getType(),
                    noti.getCreatedAt(),
                    timeAgo
            );
        }
    }

    public static NotificationAllResponseDTO from(
            long notificationCount,
            boolean hasNext,
            String nextCursor,
            List<Notification> list,
            TimeAgoFormatter timeAgoFormatter
    ) {
        List<NotificationDTO> responses = list.stream()
                .map(n -> NotificationDTO.from(n, timeAgoFormatter.format(n.getCreatedAt())))
                .toList();

        return new NotificationAllResponseDTO((int) notificationCount, hasNext, nextCursor, responses);
    }
}
