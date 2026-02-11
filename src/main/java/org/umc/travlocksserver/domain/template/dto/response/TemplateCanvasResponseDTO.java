package org.umc.travlocksserver.domain.template.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.enums.TripDays;

public record TemplateCanvasResponseDTO(
	Long templateId,
	String title,
	Integer dayNo,
	TripDays tripDays,
	Boolean isPublic,
	Integer vlockCount,
	Double totalHours,
	Double totalMoveHours,
	Double totalStayHours,
	List<Long> cities,
	List<TemplateCanvasVlockDTO> vlocks,
	LocalDateTime createdAt
) {
	public static TemplateCanvasResponseDTO from(
		Template template,
		Integer dayNo,
		TemplateDay day,
		double totalHours,
		double totalMoveHours,
		double totalStayHours,
		List<Long> cities,
		List<TemplateCanvasVlockDTO> vlocks
	) {
		return new TemplateCanvasResponseDTO(
			template.getId(),
			template.getTitle(),
			dayNo,
			template.getTripDays(),
			template.getIsPublic(),
			day.getVlockCount(),
			totalHours,
			totalMoveHours,
			totalStayHours,
			cities,
			vlocks,
			template.getCreatedAt()
		);
	}
}