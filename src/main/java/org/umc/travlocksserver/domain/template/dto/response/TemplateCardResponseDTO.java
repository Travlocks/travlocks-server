package org.umc.travlocksserver.domain.template.dto.response;

public record TemplateCardResponseDTO(
	Long templateId,
	String coverImgUrl,
	String title,
	Long travelThemeId,
	String travelTheme,
	Long memberId,
	String ownerNickname,
	Double rating,
	Integer favoriteCount) {
}
