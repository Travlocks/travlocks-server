package org.umc.travlocksserver.domain.templatevlock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.templateday.entity.TemplateDay;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "template_vlocks")
public class TemplateVlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_vlocks_id")
    private Long id;

    /** 어느 DAY에 속한 블록인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_day_id", nullable = false)
    private TemplateDay templateDay;

    /** 어떤 블록인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vlock_id", nullable = false)
    private Vlock vlock;

    /** DAY 내 순서 */
    @Column(name = "order_no", nullable = false)
    private Integer orderNo;

    /** 체류 시간 (분) */
    @Column(name = "stay_minutes", nullable = false)
    private Integer stayMinutes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
