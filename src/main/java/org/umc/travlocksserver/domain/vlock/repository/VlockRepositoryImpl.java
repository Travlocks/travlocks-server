package org.umc.travlocksserver.domain.vlock.repository;

import java.util.List;

import org.umc.travlocksserver.domain.location.dto.CityDTO;
import org.umc.travlocksserver.domain.location.dto.RegionDTO;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockResponseDTO;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockCategoryDTO;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import static org.umc.travlocksserver.domain.vlock.entity.QVlock.vlock;
import static org.umc.travlocksserver.domain.vlock.entity.QVlockCategory.vlockCategory;
import static org.umc.travlocksserver.domain.location.entity.QCity.city;
import static org.umc.travlocksserver.domain.location.entity.QRegion.region;
import static org.umc.travlocksserver.global.aws.S3Path.S3_DOMAIN;

@RequiredArgsConstructor
public class VlockRepositoryImpl implements VlockRepositoryCustom {

	private static final int POPULAR_VLOCK_LIMIT = 20;

	private final JPAQueryFactory queryFactory;

	/** 인기 블록 조회 */
	@Override
	public List<VlockResponseDTO> findPopularVlocks(Long cityId) {
		return baseQuery(cityId, categoryDefaultImage())
			.orderBy(vlock.usageCount.desc(), vlock.id.desc())
			.limit(POPULAR_VLOCK_LIMIT)
			.fetch();
	}

	/** 카테고리 블록 조회 */
	@Override
	public List<VlockResponseDTO> findCategoryVlocks(Long cityId, Long categoryId) {
		return baseQuery(cityId, categoryDefaultImage())
			.where(vlockCategory.id.eq(categoryId))
			.orderBy(vlock.usageCount.desc(), vlock.id.desc())
			.fetch();
	}

	private StringExpression categoryDefaultImage() {
		return new CaseBuilder()
			.when(vlock.coverImgUrl.isNull())
			.then(vlockCategory.defaultCategoryImageKey.prepend(S3_DOMAIN))
			.otherwise(vlock.coverImgUrl);
	}

	private JPAQuery<VlockResponseDTO> baseQuery(Long cityId, StringExpression coverImgUrl) {
		return queryFactory
			.select(vlockResponseProjection(coverImgUrl))
			.from(vlock)
			.join(vlock.vlockCategory, vlockCategory)
			.join(vlock.city, city)
			.join(city.region, region)
			.where(
				city.id.eq(cityId),
				vlock.deletedAt.isNull(),
				vlock.isPublic.isTrue()
			);
	}

	private ConstructorExpression<VlockResponseDTO> vlockResponseProjection(StringExpression coverImgUrl) {
		return Projections.constructor(VlockResponseDTO.class,
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
			coverImgUrl,
			vlock.linkUrl,

			vlock.latitude,
			vlock.longitude,

			vlock.usageCount,

			vlock.isPublic,

			vlock.createdAt,
			vlock.updatedAt
		);
	}
}
