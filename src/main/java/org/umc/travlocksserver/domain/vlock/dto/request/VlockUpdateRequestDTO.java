package org.umc.travlocksserver.domain.vlock.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VlockUpdateRequestDTO(
	@NotBlank(message = "이름은 필수입니다.")
	String name,

	@NotBlank(message = "주소는 필수입니다.")
	String address,

	@NotNull(message = "Category ID는 필수입니다.")
	Long categoryId,

	@NotNull(message = "City ID는 필수입니다.")
	Long cityId,

	String memo,

	Double latitude,
	Double longitude,

	Boolean isPublic,
	Boolean deleteCoverImg) {
}
