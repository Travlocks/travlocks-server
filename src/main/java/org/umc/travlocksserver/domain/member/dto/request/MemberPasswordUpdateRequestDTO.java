package org.umc.travlocksserver.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberPasswordUpdateRequestDTO(
        @NotBlank String currentPassword,
        @NotBlank String newPassword
) {
}
