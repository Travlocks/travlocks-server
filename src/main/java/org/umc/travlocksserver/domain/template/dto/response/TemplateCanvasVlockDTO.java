package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.enums.ConnectionPortType;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockBriefDTO;

public record TemplateCanvasVlockDTO(
	Long templateVlockId,
	Integer orderNo,
	Double stayHours,
	Double canvasX,
	Double canvasY,
	ConnectionPortType inputPort,
	ConnectionPortType outputPort,
	Integer nextMoveMinutes,
	VlockBriefDTO vlock
) {
	public static TemplateCanvasVlockDTO from(TemplateVlock cur, int nextMoveMinutes) {
		return new TemplateCanvasVlockDTO(
			cur.getId(),
			cur.getOrderNo(),
			cur.getStayHours(),
			cur.getCanvasX(),
			cur.getCanvasY(),
			cur.getInputPort(),
			cur.getOutputPort(),
			nextMoveMinutes,
			new VlockBriefDTO(
				cur.getVlock().getId(),
				cur.getVlock().getName(),
				cur.getVlock().getVlockCategory().getName()
			)
		);
	}
}
