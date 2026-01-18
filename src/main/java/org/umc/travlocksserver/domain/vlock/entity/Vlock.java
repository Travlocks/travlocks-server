package org.umc.travlocksserver.domain.vlock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.city.entity.City;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.vlockcategory.entity.VlockCategory;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "vlocks")
public class Vlock extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vlock_id")
    private Long id;

    /** 블록 카테고리 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vlock_category_id", nullable = false)
    private VlockCategory vlockCategory;

    /** 도시 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    /** 소유자 (외부 API 블록은 NULL 가능) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(name = "cover_img_url", nullable = false, length = 255)
    private String coverImgUrl;

    @Column(nullable = false)
    private Double latitude;

    @Column(name = "longtitude", nullable = false)
    private Double longitude;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 200)
    private String memo;

    @Column(name = "link_url", length = 255)
    private String linkUrl;

    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;
}
