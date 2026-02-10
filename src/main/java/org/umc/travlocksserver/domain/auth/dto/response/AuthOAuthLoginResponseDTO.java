package org.umc.travlocksserver.domain.auth.dto.response;

import org.umc.travlocksserver.domain.member.enums.MemberStatus;

public record AuthOAuthLoginResponseDTO(
        Long memberId,
        MemberStatus status,
        String accessToken,
        long accessTokenExpiresIn
) {}
