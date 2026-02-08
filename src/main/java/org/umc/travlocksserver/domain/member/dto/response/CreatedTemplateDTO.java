package org.umc.travlocksserver.domain.member.dto.response;

import java.time.LocalDateTime;

public record CreatedTemplateDTO(
        Long templateId,
        String templateTitle,
        Long regionId,
        LocalDateTime createdAt,
        boolean isFavorite
) {}