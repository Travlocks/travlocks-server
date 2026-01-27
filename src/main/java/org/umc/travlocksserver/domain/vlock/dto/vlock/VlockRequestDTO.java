package org.umc.travlocksserver.domain.vlock.dto.vlock;

import jakarta.validation.constraints.NotBlank;

public record VlockRequestDTO(
	@NotBlank(message = "이름은 필수입니다.")
	String name,

	@NotBlank(message = "주소는 필수입니다.")
	String address,

	Long categoryId,
	Long cityId,

	String memo,

	Double latitude,
	Double longitude
) {
}
