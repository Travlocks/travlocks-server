package org.umc.travlocksserver.domain.template.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplateSuccessCode implements BaseCode {

	TEMPLATE_REMIX_SUCCESS(
		HttpStatus.CREATED,
		"템플릿 리믹스(복제)에 성공했습니다."
	);

	private final HttpStatus status;
	private final String message;
}
