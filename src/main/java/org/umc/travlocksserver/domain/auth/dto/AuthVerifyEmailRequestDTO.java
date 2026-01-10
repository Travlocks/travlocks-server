package org.umc.travlocksserver.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthVerifyEmailRequestDTO(
        @NotBlank(message = "verificationId는 필수입니다.")
        String verificationId,

        @NotBlank(message = "code는 필수입니다.")
        String code
) {}
