package org.umc.travlocksserver.domain.template.dto.response;

import java.util.List;

public record TemplateDetailResponseDTO(
        Long templateId,
        String title,
        String cityName,
        String theme,
        String ownerProfileImage,
        String ownerNickname,
        String coverImageUrl,
        Long memberId,
        Double rating,
        String tripDays,
        Integer remixCount,
        String description,
        List<String> tags,
        List<BlockDTO> blocks
) {
    public record BlockDTO(
            Long blockId,
            String name,
            Double latitude,
            Double longitude,
            String address
    ) {}
}