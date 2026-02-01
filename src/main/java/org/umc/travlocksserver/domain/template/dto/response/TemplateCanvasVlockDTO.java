package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.vlock.dto.response.VlockBriefDTO;

public record TemplateCanvasVlockDTO(
	Long templateVlockId,
	Integer orderNo,
	Integer stayMinutes,
	Integer nextMoveMinutes,
	VlockBriefDTO vlock
) {
}