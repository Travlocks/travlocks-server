package org.umc.travlocksserver.domain.notification.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.notification.dto.response.NotificationAllResponseDTO;
import org.umc.travlocksserver.domain.notification.entity.Notification;
import org.umc.travlocksserver.domain.notification.repository.NotificationRepository;
import org.umc.travlocksserver.global.util.TimeAgoFormatter;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final TimeAgoFormatter timeAgoFormatter;

    public NotificationAllResponseDTO getNotifications(Long memberId, String cursor, Integer size) {
        Pageable pageable = PageRequest.of(0, size);

        List<Notification> notifications = (cursor == null || cursor.isBlank())
                ? notificationRepository.findFirstPage(memberId, pageable)
                : notificationRepository.findNextPage(memberId, LocalDateTime.parse(cursor), pageable);

        boolean hasNext = notifications.size() == size;  // 이번에 가져온 데이터 수가 size와 같으면 다음에 데이터가 더 있을 수 있음
        String nextCursor = hasNext ? notifications.get(notifications.size() - 1).getCreatedAt().toString() : null;

        return NotificationAllResponseDTO.from(hasNext, nextCursor, notifications, timeAgoFormatter);
    }
}
