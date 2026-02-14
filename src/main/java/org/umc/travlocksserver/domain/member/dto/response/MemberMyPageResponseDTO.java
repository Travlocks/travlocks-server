package org.umc.travlocksserver.domain.member.dto.response;

import java.util.List;

public record MemberMyPageResponseDTO(
	Long memberId,
	String nickname,
	String introduction,
	String profileImageUrl,
	String email,
	List<Long> preferredTravelStyleIds,
	List<Long> preferredTravelThemeIds,
	Counts counts,
	Recent recent) {
	public record Counts(
		int vlockCount,
		int templateCount,
		int starCount) {
	}

	public record Recent(
		List<MyPageRecentVlockDTO> myPageRecentVlocks,
		List<MyPageRecentTemplateDTO> myPageRecentTemplates) {
	}
}
