package org.umc.travlocksserver.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthOAuthNaverLoginRequestDTO(
	@NotBlank
	String code,
	@NotBlank
	String state) {
}
