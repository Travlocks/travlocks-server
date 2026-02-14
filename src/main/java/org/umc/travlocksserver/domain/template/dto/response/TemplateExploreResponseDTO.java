package org.umc.travlocksserver.domain.template.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record TemplateExploreResponseDTO(
	Long templateId,
	String title,
	String coverImageUrl,
	Long ownerId,
	String ownerNickname,
	String travelTheme,
	Double avgRating,
	Integer remixCount) {
	@QueryProjection
	public TemplateExploreResponseDTO {
	}
}
