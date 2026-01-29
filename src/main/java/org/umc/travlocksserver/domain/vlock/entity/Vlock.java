package org.umc.travlocksserver.domain.vlock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Column(nullable = false)
    private String coverImgUrl;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String address;

    @Column(length = 200)
    private String memo;

    private String linkUrl;

    @Column(nullable = false)
    private Integer usageCount;

    @Column(nullable = false)
    private Boolean isPublic;

	@Builder(access = AccessLevel.PRIVATE)
	private Vlock(
		VlockCategory vlockCategory,
		City city,
		Member owner,
		String name,
		Double latitude,
		Double longitude,
		String address,
		String memo
	) {
		this.vlockCategory = vlockCategory;
		this.city = city;
		this.owner = owner;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
		this.memo = memo;
		this.coverImgUrl = "";
		this.linkUrl = "";
		this.usageCount = 0;
		this.isPublic = false;
	}

	public static Vlock create(
		VlockCategory category,
		City city,
		Member owner,
		String name,
		Double latitude,
		Double longitude,
		String address,
		String memo
	) {
		return Vlock.builder()
			.vlockCategory(category)
			.city(city)
			.owner(owner)
			.name(name)
			.latitude(latitude)
			.longitude(longitude)
			.address(address)
			.memo(memo)
			.build();
	}
}
