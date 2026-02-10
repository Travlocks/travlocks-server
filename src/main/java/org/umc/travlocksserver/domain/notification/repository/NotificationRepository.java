package org.umc.travlocksserver.domain.notification.repository;

import org.springframework.data.domain.Pageable;
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

    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.receiverId = :receiverId
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findFirstPage(Long receiverId, Pageable pageable);

    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.receiverId = :receiverId
          AND n.createdAt < :cursor
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findNextPage(Long receiverId, LocalDateTime cursor, Pageable pageable);
}
