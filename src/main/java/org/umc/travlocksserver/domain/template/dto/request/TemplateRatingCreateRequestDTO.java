package org.umc.travlocksserver.domain.template.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record TemplateRatingCreateRequestDTO(
	@NotNull
	@DecimalMin(value = "1.0", inclusive = true, message = "평점은 1.0 이상이어야 합니다.")
	@DecimalMax(value = "5.0", inclusive = true, message = "평점은 5.0 이하여야 합니다.")
	Double rating,
	String content
) {
}