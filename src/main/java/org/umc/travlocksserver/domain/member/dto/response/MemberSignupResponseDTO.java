package org.umc.travlocksserver.domain.member.dto.response;

public record MemberSignupResponseDTO(
        Long memberId,
        String nickname,
        String accessToken,
        Long accessTokenExpiresIn,
        String profileImageUrl
) {}
