package org.umc.travlocksserver.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "template_vlocks")
public class TemplateVlock extends BaseEntity {

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

    /** 체류 시간 (시간) */
    @Column(name = "stay_hours", nullable = false)
    private Double stayHours;

    public void updateOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }
}
