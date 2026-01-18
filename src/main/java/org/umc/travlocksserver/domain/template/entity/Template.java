package org.umc.travlocksserver.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.traveltheme.entity.TravelTheme;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "templates")
public class Template extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long id;

    /** 리믹스된 경우 부모 템플릿 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_template_id")
    private Template parentTemplate;

    /** 템플릿 소유자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    /** 여행 테마 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_theme_id", nullable = false)
    private TravelTheme travelTheme;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", nullable = false, length = 255)
    private String coverImageUrl;

    @Column(name = "transport_type", nullable = false, length = 10)
    private String transportType;

    @Column(name = "progress_rate", nullable = false)
    private Integer progressRate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "trip_days", nullable = false, length = 20)
    private String tripDays;

    @Column(name = "vlock_count", nullable = false)
    private Integer vlockCount;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "share_token", nullable = false, length = 100)
    private String shareToken;

    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount;

    @Column(name = "remix_count", nullable = false)
    private Integer remixCount;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount;

    @Column(name = "avg_rating", nullable = false)
    private Double avgRating;
}
