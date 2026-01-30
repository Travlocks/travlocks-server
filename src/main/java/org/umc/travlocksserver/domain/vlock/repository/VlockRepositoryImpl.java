package org.umc.travlocksserver.domain.vlock.repository;

import java.util.List;

import org.umc.travlocksserver.domain.location.dto.CityDTO;
import org.umc.travlocksserver.domain.location.dto.RegionDTO;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockResponseDTO;
import org.umc.travlocksserver.domain.vlock.dto.vlockCategory.VlockCategoryDTO;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import static org.umc.travlocksserver.domain.vlock.entity.QVlock.vlock;
import static org.umc.travlocksserver.domain.vlock.entity.QVlockCategory.vlockCategory;
import static org.umc.travlocksserver.domain.location.entity.QCity.city;
import static org.umc.travlocksserver.domain.location.entity.QRegion.region;

@RequiredArgsConstructor
public class VlockRepositoryImpl implements VlockRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	/** 인기 블록 조회 */
	@Override
	public List<VlockResponseDTO> findAllByCityIdOrderByUsageCountDesc(Long cityId) {
		return queryFactory
			.select(Projections.constructor(VlockResponseDTO.class,
				vlock.id,
				vlock.owner.id,

				Projections.constructor(VlockCategoryDTO.class,
					vlockCategory.id,
					vlockCategory.name,
					vlockCategory.stayHours
				),

				Projections.constructor(CityDTO.class,
					city.id,
					city.name,

					Projections.constructor(RegionDTO.class,
						region.id,
						region.name
					)
				),

				vlock.name,
				vlock.address,
				vlock.memo,
				vlock.coverImgUrl,
				vlock.linkUrl,

				vlock.latitude,
				vlock.longitude,

				vlock.usageCount,

				vlock.isPublic
			))
			.from(vlock)
			.join(vlock.vlockCategory, vlockCategory)
			.join(vlock.city, city)
			.join(city.region, region)
			.where(
				city.id.eq(cityId),
				vlock.deletedAt.isNull(),
				vlock.isPublic.isTrue()
			)
			.orderBy(vlock.usageCount.desc())
			.limit(20)
			.fetch();
	}
}
