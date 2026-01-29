package org.umc.travlocksserver.domain.template.dto.response;

import java.util.List;

public record TemplateCursorResponseDTO(
	Long nextCursor,
	boolean hasNext,
	List<TemplateSummaryDTO> items
) {
}