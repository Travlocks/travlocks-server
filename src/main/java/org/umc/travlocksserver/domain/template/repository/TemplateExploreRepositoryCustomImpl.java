package org.umc.travlocksserver.domain.template.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.dto.response.TemplateExploreResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.QTemplateExploreResponseDTO;
import org.umc.travlocksserver.domain.template.entity.QTemplate;
import org.umc.travlocksserver.domain.member.entity.QMember;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.enums.TripDays;
import org.umc.travlocksserver.domain.traveltheme.entity.QTravelTheme;
import org.umc.travlocksserver.domain.template.entity.QTemplateCity;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class TemplateExploreRepositoryCustomImpl implements TemplateExploreRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private static final int PAGE_SIZE = 9; // 한 페이지당 9개로 고정

	@Override
	public List<TemplateExploreResponseDTO> findExploreTemplates(
		String keyword,
		List<String> cityNames,
		List<String> travelThemes,
		List<TripDays> tripDays,
		List<String> transportTypes,
		String sort,
		int offset) {
		QTemplate t = QTemplate.template;
		QMember m = QMember.member;
		QTravelTheme tt = QTravelTheme.travelTheme;
		QTemplateCity tc = QTemplateCity.templateCity;

		// 정렬 기준
		OrderSpecifier<?> orderSpecifier = getOrderSpecifier(t, sort);

		// QueryDSL 쿼리 작성
		return queryFactory
			.select(
				new QTemplateExploreResponseDTO(
					t.id,
					t.title,
					t.coverImageUrl,
					t.owner.id,
					m.nickname,
					tt.content,
					t.avgRating,
					t.remixCount))
			.from(t)
			.join(t.owner, m)
			.join(t.travelTheme, tt)
			.leftJoin(t.templateCities, tc)
			.where(
				t.isPublic.eq(true),
				keywordContains(keyword, t),
				citiesIn(cityNames, tc),
				travelThemesIn(travelThemes, tt),
				tripDaysIn(tripDays, t),
				transportTypesIn(transportTypes, t))
			.offset(offset)
			.limit(PAGE_SIZE)
			.orderBy(orderSpecifier)
			.fetch();
	}

	private com.querydsl.core.types.Predicate tripDaysIn(List<TripDays> tripDays, QTemplate t) {
		if (tripDays == null || tripDays.isEmpty())
			return null;

		Set<TripDays> dbValues = new HashSet<>(tripDays);

		return t.tripDays.in(dbValues);
	}

	private com.querydsl.core.types.Predicate transportTypesIn(List<String> transportTypes, QTemplate t) {
		if (transportTypes == null || transportTypes.isEmpty()) {
			return null;
		}

		List<TransportType> enums = new ArrayList<>();
		for (String type : transportTypes) {
			switch (type) {
				case "도보" -> enums.add(TransportType.WALK);
				case "차량" -> enums.add(TransportType.CAR);
				case "대중교통" -> enums.add(TransportType.TRANSIT);
			}
		}
		return t.transportType.in(enums);
	}

	private OrderSpecifier<?> getOrderSpecifier(QTemplate t, String sort) {
		if ("최신순".equals(sort)) {
			return t.createdAt.desc();
		} else if ("인기순".equals(sort)) {
			return t.remixCount.desc();
		} else if ("별점순".equals(sort)) {
			return t.avgRating.desc();
		} else {
			return t.avgRating.desc(); //기본값 별점순
		}
	}

	private com.querydsl.core.types.Predicate keywordContains(String keyword, QTemplate t) {
		if (keyword == null || keyword.isEmpty()) {
			return null;
		}
		return t.title.containsIgnoreCase(keyword);
	}

	private com.querydsl.core.types.Predicate citiesIn(List<String> cityNames, QTemplateCity tc) {
		if (cityNames == null || cityNames.isEmpty()) {
			return null;
		}
		BooleanBuilder builder = new BooleanBuilder();

		for (String cityName : cityNames) {
			switch (cityName) {
				case "서울" -> builder.or(tc.city.id.between(101, 111));
				case "경기" -> builder.or(tc.city.id.between(201, 210));
				case "인천" -> builder.or(tc.city.id.between(301, 307));
				case "강원" -> builder.or(tc.city.id.between(401, 410));
				case "충청" -> builder.or(tc.city.id.between(501, 510));
				case "전라" -> builder.or(tc.city.id.between(601, 610));
				case "경상" -> builder.or(tc.city.id.between(701, 710));
				case "제주" -> builder.or(tc.city.id.between(801, 809));
			}
		}

		return builder;
	}

	private com.querydsl.core.types.Predicate travelThemesIn(List<String> travelThemes, QTravelTheme tt) {
		if (travelThemes == null || travelThemes.isEmpty()) {
			return null;
		}
		return tt.content.in(travelThemes);
	}
}
