package org.umc.travlocksserver.domain.template.dto.request;

import jakarta.validation.constraints.Size;

public record TemplateSaveRequestDTO(

	@Size(max = 30, message = "제목은 30자 이내로 입력해주세요.")
	String title,

	String description,

	Boolean isPublic) {
}
