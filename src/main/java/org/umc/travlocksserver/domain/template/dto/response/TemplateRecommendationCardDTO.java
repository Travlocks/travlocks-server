package org.umc.travlocksserver.domain.template.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import org.umc.travlocksserver.domain.template.enums.TripDays;

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
    public TemplateRecommendationCardDTO(
            Long templateId,
            String coverImgUrl,
            String title,
            String description,
            String region,
            TripDays tripDays,
            String tripTheme,
            Double totalScore
    ) {
        this(
                templateId,
                coverImgUrl,
                title,
                description,
                region,
                tripDays != null ? tripDays.getDescription() : null,
                tripTheme,
                totalScore
        );
    }
}