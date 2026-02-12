package org.umc.travlocksserver.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberPasswordUpdateRequestDTO(
	@NotBlank
	String currentPassword,

	@NotBlank @Size(min = 8, max = 20) @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Za-z])[A-Za-z\\d!@#$%^&*()_+=\\-]{8,20}$", message = "비밀번호는 영문과 숫자를 반드시 포함해야 하며, 특수문자는 선택적으로 사용할 수 있습니다.")
	String newPassword) {
}
