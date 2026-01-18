package org.umc.travlocksserver.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthResendEmailRequestDTO(
	@NotBlank(message = "verificationId는 필수입니다.")
	String verificationId) {
}
