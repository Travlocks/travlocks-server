package org.umc.travlocksserver.domain.template.dto.response;

import java.time.LocalDateTime;

public record TemplateSummaryDTO(
	Long templateId,
	String title,
	String coverImageUrl,
	Integer favoriteCount,
	Double avgRating,
	LocalDateTime createdAt
) {
}