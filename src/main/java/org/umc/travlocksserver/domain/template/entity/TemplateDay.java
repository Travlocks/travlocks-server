package org.umc.travlocksserver.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.global.entity.CreatedBaseEntity;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "template_days")
public class TemplateDay extends CreatedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_day_id")
    private Long id;

    /** 어떤 템플릿의 Day인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    /** DAY 1, DAY 2, ... */
    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    /** 하루 시작 시간 */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "vlock_count", nullable = false)
    private Integer vlockCount;
}
