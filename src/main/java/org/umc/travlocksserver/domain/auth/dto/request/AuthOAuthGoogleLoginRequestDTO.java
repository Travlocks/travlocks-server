package org.umc.travlocksserver.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthOAuthGoogleLoginRequestDTO(
        @NotBlank String idToken
) {}
