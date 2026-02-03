package org.umc.travlocksserver.domain.template.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TemplateErrorCode implements BaseCode {

	TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 템플릿입니다."),
	TEMPLATE_CANVAS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 템플릿 캔버스입니다."),
    TEMPLATE_NOT_PUBLIC(HttpStatus.BAD_REQUEST, "공개되지 않은 템플릿입니다."),
	TEMPLATE_DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 템플릿의 일차(dayNo)가 존재하지 않습니다."),
	UNSUPPORTED_TRANSPORT_TYPE(HttpStatus.BAD_REQUEST, "현재 지원하지 않는 이동 수단입니다."),
	TEMPLATE_NO_MATCH(HttpStatus.NOT_FOUND, "조건에 맞는 템플릿이 없어요."),
	TEMPLATE_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 다시 시도해주세요.");


	private final HttpStatus status;
	private final String message;
}
