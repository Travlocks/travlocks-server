package org.umc.travlocksserver.domain.member.dto.response;

import java.time.LocalDateTime;

public record CreatedTemplateDTO(
        Long templateId,
        String title,
        String city,
        LocalDateTime createdAt,
        boolean isFavorite
) {}