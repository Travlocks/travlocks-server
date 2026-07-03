package org.umc.travlocksserver.domain.notification.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.service.query.MemberQueryService;
import org.umc.travlocksserver.domain.notification.dto.response.NotificationAllResponseDTO;
import org.umc.travlocksserver.domain.notification.entity.Notification;
import org.umc.travlocksserver.domain.notification.repository.NotificationRepository;
import org.umc.travlocksserver.global.util.TimeAgoFormatter;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationQueryService {

	private final NotificationRepository notificationRepository;
	private final TimeAgoFormatter timeAgoFormatter;
	private final MemberQueryService memberQueryService;

	public NotificationAllResponseDTO getNotifications(Long memberId, String cursor, Integer size) {
		Member member = memberQueryService.getById(memberId);
		long totalCount = member.getNotificationCount();

		List<Notification> notifications = (cursor == null || cursor.isBlank())
			? notificationRepository.findFirstPage(memberId, size)
			: notificationRepository.findNextPage(memberId, LocalDateTime.parse(cursor), size);

		boolean hasNext = notifications.size() == size; // 이번에 가져온 데이터 수가 size와 같으면 다음에 데이터가 더 있을 수 있음
		String nextCursor = hasNext ? notifications.get(notifications.size() - 1).getCreatedAt().toString() : null;

		return NotificationAllResponseDTO.from(totalCount, hasNext, nextCursor, notifications, timeAgoFormatter);
	}
}
