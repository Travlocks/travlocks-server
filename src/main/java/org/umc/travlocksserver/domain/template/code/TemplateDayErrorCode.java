package org.umc.travlocksserver.domain.template.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum TemplateDayErrorCode implements BaseCode {

    TEMPLATE_DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 템플릿 Day 입니다."),
    TEMPLATE_VLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 블록입니다."),
    TEMPLATE_VLOCK_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "하루 권장 블록 개수는 4개입니다.")
            ;

    private final HttpStatus status;
    private final String message;
}
