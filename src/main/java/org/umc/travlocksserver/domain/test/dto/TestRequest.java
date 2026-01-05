package org.umc.travlocksserver.domain.test.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TestRequest(
	@NotBlank
	String name,

	@Min(value = 1)
	int age) {
}
