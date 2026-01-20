package org.umc.travlocksserver.domain.auth.dto.response;

public record AuthRefreshResponseDTO(
        String accessToken,
        long accessTokenExpiresIn
) {}
