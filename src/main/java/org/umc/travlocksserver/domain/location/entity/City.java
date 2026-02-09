package org.umc.travlocksserver.domain.location.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "cities")
public class City extends SoftDeleteBaseEntity {

    @Id
    @Column(name = "city_id")
    private Long id;

    // 점선 관계 → 비식별 → ManyToOne 단방향
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, length = 10)
    private String name;
}
