package org.umc.travlocksserver.domain.vlock.entity;

import jakarta.persistence.*;
import lombok.*;

import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.vlock.dto.command.VlockCreateCommand;
import org.umc.travlocksserver.domain.vlock.dto.command.VlockUpdateCommand;
import org.umc.travlocksserver.global.entity.SoftDeleteBaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "vlocks")
public class Vlock extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vlock_id")
    private Long id;

    // 외부 장소 ID(Kakao place_id 등)
    private String externalPlaceId;

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

    private String coverImgUrl;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String address;

    @Column(length = 200)
    private String memo;

    @Column(nullable = false)
    private Integer usageCount;

    @Column(nullable = false)
    private Boolean isPublic;

	public boolean isOwnedBy(Long memberId) {
		return this.owner.getId().equals(memberId);
	}

	public static Vlock create(VlockCreateCommand command) {
		return Vlock.builder()
				.vlockCategory(command.category())
				.city(command.city())
				.owner(command.owner())
				.name(command.name())
				.address(command.address())
				.coverImgUrl(command.coverImgUrl())
				.memo(command.memo())
				.latitude(command.latitude())
				.longitude(command.longitude())
				.usageCount(0)
				.isPublic(false)
				.build();
	}

	public static Vlock createByExternal(
			String externalPlaceId,
			VlockCategory vlockCategory,
			City city,
			String name,
			Double latitude,
			Double longitude,
			String address
	) {
		return Vlock.builder()
				.externalPlaceId(externalPlaceId)
				.vlockCategory(vlockCategory)
				.city(city)
				.name(name)
				.coverImgUrl(null)
				.latitude(latitude)
				.longitude(longitude)
				.address(address)
				.usageCount(0)
				.isPublic(true)
				.build();
	}

	public void update(VlockUpdateCommand command) {
		this.vlockCategory = command.category();
		this.city = command.city();
		this.name = command.name();
		this.address = command.address();
		this.latitude = command.latitude();
		this.longitude = command.longitude();
		this.coverImgUrl = command.coverImgUrl();

		if (command.memo() != null) {
			this.memo = command.memo();
		}
		if (command.isPublic() != null) {
			this.isPublic = command.isPublic();
		}
	}
}
