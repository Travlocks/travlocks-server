package org.umc.travlocksserver.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.notification.enums.NotificationType;
import org.umc.travlocksserver.global.entity.CreatedBaseEntity;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "notifications", indexes = {
	@Index(name = "idx_notifications_receiver_created_id", columnList = "receiver_id, created_at, notification_id")
})
public class Notification extends CreatedBaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long id;

	@Column(nullable = false)
	private Long receiverId;

	@Column(nullable = false)
	private Long actorId;

	@Column(length = 10, nullable = false)
	private String actorNicknameSnapshot;

	@Column(nullable = false)
	private Long templateId;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private NotificationType type;

	public static Notification create(Long receiverId, Long actorId, String actorNicknameSnapshot, Long templateId,
		NotificationType type) {
		return Notification.builder()
			.receiverId(receiverId)
			.actorId(actorId)
			.actorNicknameSnapshot(actorNicknameSnapshot)
			.templateId(templateId)
			.type(type)
			.build();
	}
}
