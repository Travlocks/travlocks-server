package org.umc.travlocksserver.domain.template.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record TemplateExploreResponseDTO(
	Long templateId,
	String title,
	String coverImageUrl,
	Long ownerId,
	String ownerNickname,
	String travelTheme,
	Double avgRating,
	Integer remixCount,
	LocalDateTime createdAt) {
	@QueryProjection
	public TemplateExploreResponseDTO {
	}
}
