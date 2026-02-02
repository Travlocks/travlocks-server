package org.umc.travlocksserver.domain.template.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum TemplateDayErrorCode implements BaseCode {

    TEMPLATE_DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 템플릿 일정입니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
