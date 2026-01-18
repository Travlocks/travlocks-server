package org.umc.travlocksserver.domain.member.entity.mapping;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.entity.TravelTheme;

import java.time.LocalDateTime;

@Entity
@Table(name = "preferred_travel_themes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_theme", columnNames = {"member_id", "travel_theme_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PreferredTravelTheme {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preferred_travel_theme_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "travel_theme_id", nullable = false)
    private TravelTheme travelTheme;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
