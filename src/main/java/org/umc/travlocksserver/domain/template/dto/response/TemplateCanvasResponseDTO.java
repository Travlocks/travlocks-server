package org.umc.travlocksserver.domain.template.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record TemplateCanvasResponseDTO(
	Long templateId,
	String title,
	Integer dayNo,
	Integer vlockCount,
	Double totalHours,
	Double totalMoveHours,
	Double totalStayHours,
	List<Long> cities,
	List<TemplateCanvasVlockDTO> vlocks,
	LocalDateTime createdAt
) {
}