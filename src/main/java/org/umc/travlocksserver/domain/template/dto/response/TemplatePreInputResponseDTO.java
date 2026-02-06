package org.umc.travlocksserver.domain.template.dto.response;

public record TemplatePreInputResponseDTO(
        Long templateId,
        Integer dayCount,
        Boolean isPublic,
        String shareToken,
        String shareUrl
) {
    public static TemplatePreInputResponseDTO of(
            Long templateId,
            Integer dayCount,
            String shareToken
    ) {
        String shareUrl = "https://travlocks.com/share/" + shareToken;

        return new TemplatePreInputResponseDTO(
                templateId,
                dayCount,
                false,  // 초기값 항상 비공개
                shareToken,
                shareUrl
        );
    }
}