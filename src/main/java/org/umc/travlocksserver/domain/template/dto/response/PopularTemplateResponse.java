package org.umc.travlocksserver.domain.template.dto.response;

import lombok.Builder;

@Builder
public record PopularTemplateResponse(
        Long templateId,
        String coverImageUrl,
        String title,
        Double avgRating,
        Integer remixCount,
        String travelTheme,
        String ownerNickname
) {
}