package org.umc.travlocksserver.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthPasswordResetLinkRequestDTO(
	@NotBlank(message = "이메일은 필수입니다.") @Email(message = "이메일 형식이 올바르지 않습니다.")
	String email) {
}
