package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.template.entity.Template;

import java.time.LocalDateTime;

public record TemplateSaveResponseDTO(
	Long templateId,
	String title,
	String description,
	String coverImageUrl,
	Boolean isPublic,
	String shareToken,
	LocalDateTime updatedAt) {
	public static TemplateSaveResponseDTO from(Template template) {
		return new TemplateSaveResponseDTO(
			template.getId(),
			template.getTitle(),
			template.getDescription(),
			template.getCoverImageUrl(),
			template.getIsPublic(),
			template.getShareToken(),
			template.getUpdatedAt());
	}
}
