package org.umc.travlocksserver.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.traveltheme.entity.TravelTheme;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "cover_image_url", nullable = true, length = 255)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false, length = 10)
    private TransportType transportType; // WALK, CAR, TRANSIT

    @Builder.Default
    @Column(name = "progress_rate", nullable = false)
    private Integer progressRate = 0;

    @Column(name = "trip_days", nullable = false, length = 20)
    private String tripDays;

    @Builder.Default
    @Column(name = "vlock_count", nullable = false)
    private Integer vlockCount = 0;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "share_token", nullable = false, length = 100)
    private String shareToken;

    @Builder.Default
    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount = 0;

    @Builder.Default
    @Column(name = "remix_count", nullable = false)
    private Integer remixCount = 0;

    @Builder.Default
    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;

    @Builder.Default
    @Column(name = "avg_rating", nullable = false)
    private Double avgRating = 0.0;

    @Builder.Default
    @Column(nullable = false)
    private int tagVersion = 0;

	public void increaseRemixCount() {
		this.remixCount = (this.remixCount == null ? 0 : this.remixCount) + 1;
	}

    public void increaseTagVersion() {
        this.tagVersion++;
    }

	public void updateAvgRating(Double avgRating) {
		this.avgRating = avgRating;
	}

	public static Template remixOf(Template original, Member remixer, String shareToken) {
		return Template.builder()
			.parentTemplate(original)
			.owner(remixer)
			.travelTheme(original.getTravelTheme())
			.title(original.getTitle() + " (리믹스)")
			.description(original.getDescription())
			.coverImageUrl(original.getCoverImageUrl())
			.transportType(original.getTransportType())
			.tripDays(original.getTripDays())
			.vlockCount(original.getVlockCount())

			// 초기화
			.progressRate(0)
			.isPublic(true)
			.shareToken(shareToken)
			.favoriteCount(0)
			.remixCount(0)
			.ratingCount(0)
			.avgRating(0.0)
            .tagVersion(0)
			.build();
	}

    @OneToMany(mappedBy = "template")
    private List<TemplateDay> templateDays = new ArrayList<>();

    @OneToMany(mappedBy = "template")
    private List<TemplateTag> templateTags = new ArrayList<>();

    @OneToMany(mappedBy = "template")
    private List<TemplateCity> templateCities = new ArrayList<>();

    public void increaseFavoriteCount() {
        this.favoriteCount++;
    }

    public void decreaseFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }
}
