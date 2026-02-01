package org.umc.travlocksserver.domain.template.dto.response;

public record TemplateRemixResponseDTO(
	Long parentTemplateId,
	Long remixedTemplateId
) {
}