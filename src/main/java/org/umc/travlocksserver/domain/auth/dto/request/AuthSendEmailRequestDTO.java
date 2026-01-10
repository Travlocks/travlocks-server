package org.umc.travlocksserver.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthSendEmailRequestDTO(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바르지 않은 이메일 형식입니다.")
        String email
) {}
