package org.umc.travlocksserver.domain.vlockcategory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "vlock_categories")
public class VlockCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vlock_category_id")
    private Long id;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(name = "stay_minutes", nullable = false)
    private Integer stayMinutes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // DDL이 NOT NULL이면 nullable=false로 맞추는 게 1차 매핑에선 안전
    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;
}
