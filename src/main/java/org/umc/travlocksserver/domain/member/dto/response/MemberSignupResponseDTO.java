package org.umc.travlocksserver.domain.member.dto.response;

import java.util.List;

public record MemberSignupResponseDTO(
	Long memberId,
	String nickname,
	String accessToken,
	Long accessTokenExpiresIn,
	String profileImageUrl,
	List<PreferredThemeItem> preferredTravelThemes,
	List<PreferredStyleItem> preferredTravelStyles) {
	public record PreferredThemeItem(Long themeId, String content) {
	}
	public record PreferredStyleItem(Long styleId, String content) {
	}
}
