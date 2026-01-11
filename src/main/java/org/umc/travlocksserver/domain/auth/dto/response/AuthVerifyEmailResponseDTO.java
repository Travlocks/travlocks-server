package org.umc.travlocksserver.domain.auth.dto.response;

public record AuthVerifyEmailResponseDTO(
        String signupToken
) {}
