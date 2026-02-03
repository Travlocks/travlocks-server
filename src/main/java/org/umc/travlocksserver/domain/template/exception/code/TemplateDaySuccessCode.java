package org.umc.travlocksserver.domain.template.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum TemplateDaySuccessCode implements BaseCode {

    VLOCK_SUGGESTION_SUCCESS(HttpStatus.OK, "블록 추천이 완료되었습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
