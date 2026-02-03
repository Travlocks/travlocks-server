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
	TEMPLATE_RECENT_NOT_FOUND(HttpStatus.NOT_FOUND, "최근 편집된 템플릿이 존재하지 않습니다.");

	private final HttpStatus status;
	private final String message;
}
