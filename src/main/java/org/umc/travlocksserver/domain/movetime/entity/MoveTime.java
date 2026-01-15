package org.umc.travlocksserver.domain.movetime.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.global.entity.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "move_times")
public class MoveTime extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "move_times_id")
    private Long id;

    /** 출발 블록 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_vlock_id", nullable = false)
    private Vlock fromVlock;

    /** 도착 블록 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_vlock_id", nullable = false)
    private Vlock toVlock;

    @Column(name = "move_minutes", nullable = false)
    private Integer moveMinutes;

    @Column(name = "transport_type", nullable = false, length = 10)
    private String transportType; // WALK, CAR, TRANSIT

    @Column(name = "distance_meter", nullable = false)
    private Integer distanceMeter;

    @Column(columnDefinition = "TEXT")
    private String polyline;
}
