package org.umc.travlocksserver.domain.template.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.location.entity.QCity;
import org.umc.travlocksserver.domain.location.entity.QRegion;
import org.umc.travlocksserver.domain.template.dto.response.QTemplateRecommendationCardDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationCardDTO;
import org.umc.travlocksserver.domain.template.entity.QTemplate;
import org.umc.travlocksserver.domain.template.entity.QTemplateCity;
import org.umc.travlocksserver.domain.traveltheme.entity.QTravelTheme;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TemplateRepositoryCustomImpl implements TemplateRepositoryCustom {
    private static final int PREFERRED_THEME_BONUS = 30;
    private static final int RECENT_THEME_BONUS = 20;
    private static final double RATING_WEIGHT = 5.0;  // 평점은 5점 만점이므로 5 * 5 = 최대 25점
    private static final double REMIX_WEIGHT = 15.0;
    private static final double FAVORITE_WEIGHT = 10.0;

    private final JPAQueryFactory query;

    /**
    * 개인화된 템플릿 추천 목록 조회
    * 점수 계산:
    * - 선호 테마 일치: + 30점
    * - 최근 수정한 템플릿의 테마와 일치: + 20점
    * - 평점: 0~25점 (가중치 5)
    * - 리믹스 점수: 0~30점 (가중치 15)
    * - 즐겨찾기 점수: 0~10점 (가중치 10)
    */
    public List<TemplateRecommendationCardDTO> recommendPersonalized(
            List<Long> preferredThemeIds,
            List<Long> recentThemeIds,
            List<Long> excludedTemplateIds,
            int limit
    ) {
        QTemplate t = QTemplate.template;
        QTravelTheme tt= QTravelTheme.travelTheme;
        QCity c = QCity.city;
        QTemplateCity tc = QTemplateCity.templateCity;
        QTemplateCity tc2 = new QTemplateCity("tc2");
        QRegion r = QRegion.region;

        JPQLQuery<Long> firstTcIdOfTemplate = getFirstTcIdOfTemplate(t, tc2);
        BooleanBuilder builder = buildCondition(t, excludedTemplateIds);
        NumberExpression<Double> totalScore = calculateTotalScore(t, preferredThemeIds, recentThemeIds);

        return query
                .select(new QTemplateRecommendationCardDTO(
                        t.id,
                        t.coverImageUrl,
                        t.title,
                        t.description,
                        r.name,
                        t.tripDays,
                        tt.content,
                        totalScore
                ))
                .from(t)
                .join(t.travelTheme, tt)
                .leftJoin(tc).on(tc.id.eq(firstTcIdOfTemplate))  // 대표 template_city_id로 left join
                .leftJoin(tc.city, c)
                .leftJoin(c.region, r)  // city에서 region을 추출해내기 위한 join
                .where(builder)
                .orderBy(totalScore.desc(), t.id.desc())
                .limit(limit)
                .fetch();

    }

    // 템플릿의 city들 중 가장 작은 id 조회 (대표도시)
    private JPQLQuery<Long> getFirstTcIdOfTemplate(QTemplate t, QTemplateCity tc) {
        return JPAExpressions
                .select(tc.id.min())
                .from(tc)
                .where(tc.template.id.eq(t.id));
    }

    // 조건식 설계 (공개 & 리믹스한 적 없는 템플릿만)
    private BooleanBuilder buildCondition(QTemplate t, List<Long> excludedTemplateIds) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(t.isPublic.isTrue());

        if (!excludedTemplateIds.isEmpty()) {
            builder.and(t.id.notIn(excludedTemplateIds));
        }

        return builder;
    }

    // 점수 계산
    private NumberExpression<Double> calculateTotalScore(
            QTemplate t,
            List<Long> preferredThemeIds,
            List<Long> recentThemeIds
    ) {
        NumberExpression<Integer> preferredBonus = calculatePreferredBonus(t, preferredThemeIds);
        NumberExpression<Integer> recentBonus = calculateRecentBonus(t, recentThemeIds);

        NumberExpression<Double> ratingScore = t.avgRating.coalesce(0.0).multiply(RATING_WEIGHT);
        NumberExpression<Double> remixScore = log10(t.remixCount.coalesce(0).add(1).doubleValue()).multiply(REMIX_WEIGHT);
        NumberExpression<Double> favoriteScore = log10(t.favoriteCount.coalesce(0).add(1).doubleValue()).multiply(FAVORITE_WEIGHT);

        return preferredBonus.add(recentBonus).doubleValue()
                .add(ratingScore)
                .add(remixScore)
                .add(favoriteScore);
    }

    // 선호 테마와 일치하는 템플릿에 가점
    private NumberExpression<Integer> calculatePreferredBonus(QTemplate t, List<Long> preferredThemeIds) {
        return preferredThemeIds.isEmpty()
                ? Expressions.ZERO
                : new CaseBuilder()
                .when(t.travelTheme.id.in(preferredThemeIds))
                .then(PREFERRED_THEME_BONUS)
                .otherwise(0);
    }

    // 최근 수정한 템플릿 테마와 일치하는 템플릿에 가점
    private NumberExpression<Integer> calculateRecentBonus(QTemplate t, List<Long> recentThemeIds) {
        return recentThemeIds.isEmpty()
                ? Expressions.ZERO
                : new CaseBuilder()
                .when(t.travelTheme.id.in(recentThemeIds))
                .then(RECENT_THEME_BONUS)
                .otherwise(0);
    }

    // 점수 보정을 위한 log 메서드
    private NumberExpression<Double> log10(NumberExpression<Double> expression) {
        return Expressions.numberTemplate(Double.class, "LOG10({0})", expression);
    }
}
