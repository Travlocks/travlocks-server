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
	Long ownerId,
	Double rating,
	String tripDays,
	Integer remixCount,
	String description,
	List<String> tags,
	List<VlockDTO> vlocks,
	Boolean isFavorited) {
	public record VlockDTO(
		Long vlockId,
		String name,
		Double latitude,
		Double longitude,
		String address) {
	}
}
