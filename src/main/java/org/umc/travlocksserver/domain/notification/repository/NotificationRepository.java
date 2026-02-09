package org.umc.travlocksserver.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying
    @Query("""
        DELETE FROM Notification n
        WHERE n.receiverId = :receiverId
    """)
    void deleteAllByReceiverId(Long receiverId);
}
