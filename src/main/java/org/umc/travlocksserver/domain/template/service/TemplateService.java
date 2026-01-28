package org.umc.travlocksserver.domain.template.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationCardDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationsDTO;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.traveltheme.repository.PreferredTravelThemeRepository;
import org.umc.travlocksserver.infra.redis.template.CachedTemplateRecommendations;
import org.umc.travlocksserver.infra.redis.template.TemplateRecommendationCache;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TemplateService {

    private static final int RECENT_TEMPLATE_LIMIT = 5;
    private static final int RECOMMEND_TEMPLATE_LIMIT = 10;

    private final TemplateRecommendationCache cache;
    private final PreferredTravelThemeRepository preferredTravelThemeRepository;
    private final TemplateRepository templateRepository;

    public TemplateRecommendationsDTO getRecommendedTemplates(Long memberId) {
        CachedTemplateRecommendations cached = cache.get(memberId);

        // Cache hit -> Caching된 추천 리스트 반환
        if (cached != null) {
            log.info("캐싱된 데이터 반환");
            return TemplateRecommendationsDTO.from(cached);
        }

        // Cache miss -> 추천
        log.info("새로운 추천 데이터 생성");
        List<TemplateRecommendationCardDTO> templates = recommendTemplates(memberId);
        CachedTemplateRecommendations recommendedTemplates = CachedTemplateRecommendations.from(templates);
        cache.set(memberId, recommendedTemplates);
        return TemplateRecommendationsDTO.from(recommendedTemplates);
    }

    private List<TemplateRecommendationCardDTO> recommendTemplates(Long memberId) {
       // 회원 선호 테마 ID들 조회
        List<Long> preferredThemeIds = preferredTravelThemeRepository.findPreferredThemeIdsByMemberId(memberId);

        // 최근 5개 템플릿의 TravleThemeID 뽑고 Distinct
        List<Long> recentThemeIds = templateRepository.findRecentThemeIdsByMemberId(memberId, PageRequest.of(0, RECENT_TEMPLATE_LIMIT));

        // 이미 리믹스한 템플릿 ID들 조회 (추천에서 제외하기 위함)
        List<Long> excludedTemplateIds = templateRepository.findRemixedTemplateIdsByMemberId(memberId);

        // 개인화 추천
        List<TemplateRecommendationCardDTO> result = templateRepository.recommendPersonalized(preferredThemeIds, recentThemeIds,excludedTemplateIds, RECOMMEND_TEMPLATE_LIMIT);

        return result;
    }
}
