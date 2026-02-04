package org.umc.travlocksserver.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthPasswordResetConfirmRequestDTO(
        @NotBlank(message = "resetToken은 필수입니다.")
        String token,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
        String newPasswordConfirm
) {}
