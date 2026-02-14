package org.umc.travlocksserver.domain.template.dto.response;

import java.time.LocalDateTime;

public record TemplateLatestDTO(
	Long id,
	String title,
	LocalDateTime updatedAt,
	Integer progressRate,
	String regionName) {
}
