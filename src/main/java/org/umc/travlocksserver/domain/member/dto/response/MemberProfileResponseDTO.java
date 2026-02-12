package org.umc.travlocksserver.domain.member.dto.response;

import org.umc.travlocksserver.domain.template.dto.response.TemplateCardResponseDTO;
import org.umc.travlocksserver.global.response.PageResponseDTO;

public record MemberProfileResponseDTO(
	Long memberId,
	String nickname,
	String introduction,
	String profileImageUrl,
	PageResponseDTO<TemplateCardResponseDTO> templates) {
}
