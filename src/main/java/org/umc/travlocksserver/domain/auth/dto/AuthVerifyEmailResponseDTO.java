package org.umc.travlocksserver.domain.auth.dto;

public record AuthVerifyEmailResponseDTO(
        String signupToken
) {}
