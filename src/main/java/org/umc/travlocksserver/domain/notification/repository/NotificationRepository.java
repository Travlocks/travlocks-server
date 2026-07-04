package org.umc.travlocksserver.domain.notification.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	@Modifying
	@Query("""
		    DELETE FROM Notification n
		    WHERE n.receiverId = :receiverId
		""")
	void deleteAllByReceiverId(Long receiverId);

	@Query(value = """
		SELECT n.notification_id, n.receiver_id, n.actor_id, n.actor_nickname_snapshot, n.template_id, n.type, n.created_at
	    FROM notifications n
			JOIN (
				SELECT notification_id
				FROM notifications
				WHERE receiver_id = :receiverId
				ORDER BY created_at DESC, notification_id DESC
				LIMIT :limit
			) temp
		ON n.notification_id = temp.notification_id
	    ORDER BY n.created_at DESC, n.notification_id DESC
	""", nativeQuery = true
	)
	List<Notification> findFirstPage(Long receiverId, int limit);

	@Query(value = """
		SELECT n.notification_id, n.receiver_id, n.actor_id, n.actor_nickname_snapshot, n.template_id, n.type, n.created_at
		FROM notifications n
		JOIN (
			SELECT notification_id
			FROM notifications
			WHERE receiver_id = :receiverId
				AND created_at < :cursor
			ORDER BY created_at DESC, notification_id DESC
			LIMIT :limit
		) temp
		ON n.notification_id = temp.notification_id
		ORDER BY n.created_at DESC, n.notification_id DESC
	""", nativeQuery = true)
	List<Notification> findNextPage(Long receiverId, LocalDateTime cursor, int limit);
}
