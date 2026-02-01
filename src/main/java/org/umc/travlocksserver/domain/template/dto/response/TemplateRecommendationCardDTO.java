package org.umc.travlocksserver.domain.template.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record TemplateRecommendationCardDTO(
        Long templateId,
        String coverImgUrl,
        String title,
        String description,
        String region,
        String tripDays,
        String tripTheme,
        Double totalScore
) {
    @QueryProjection
    public TemplateRecommendationCardDTO {
    }
}