package org.umc.travlocksserver.domain.template.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum TemplateErrorCode implements BaseCode {

    TEMPLATE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 템플릿입니다."),
    TEMPLATE_NOT_PUBLIC(HttpStatus.BAD_REQUEST, "공개되지 않은 템플릿입니다.");

    private final HttpStatus status;
    private final String message;
}