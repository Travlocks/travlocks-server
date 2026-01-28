package org.umc.travlocksserver.infra.redis.template;

import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationCardDTO;

import java.util.List;
import java.util.UUID;

public record CachedTemplateRecommendations(
        String recommendationId,
        List<TemplateRecommendationCardDTO>  templates
) {
    public static CachedTemplateRecommendations from(List<TemplateRecommendationCardDTO> templates) {
        return new CachedTemplateRecommendations(
                UUID.randomUUID().toString(), templates
        );
    }
}
