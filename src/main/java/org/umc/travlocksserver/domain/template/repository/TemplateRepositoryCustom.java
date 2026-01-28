package org.umc.travlocksserver.domain.template.repository;

import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationCardDTO;

import java.util.List;

public interface TemplateRepositoryCustom {

    List<TemplateRecommendationCardDTO> recommendPersonalized(
            List<Long> preferredThemeIds,
            List<Long> recentThemeIds,
            List<Long> excludedTemplateIds,
            int limit
    );
}
