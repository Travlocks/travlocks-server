package org.umc.travlocksserver.domain.member.dto.response;

import org.umc.travlocksserver.domain.template.dto.response.TemplateCursorResponseDTO;

public record MemberProfileResponseDTO(
	Long memberId,
	String nickname,
	String introduction,
	String profileImageUrl,
	TemplateCursorResponseDTO templates
) {
}