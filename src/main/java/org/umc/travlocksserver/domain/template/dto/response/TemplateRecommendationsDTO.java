package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.infra.redis.template.CachedTemplateRecommendations;

import java.util.List;

public record TemplateRecommendationsDTO(
        String recommendationId,
        List<TemplateRecommendationCardDTO> templates
) {
    public static TemplateRecommendationsDTO from(CachedTemplateRecommendations cached) {
        return new TemplateRecommendationsDTO(cached.recommendationId(), cached.templates());
    }
}
