package org.umc.travlocksserver.domain.template.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum TemplateDaySuccessCode implements BaseCode {

    VLOCK_SUGGESTION_SUCCESS(HttpStatus.OK, "블록 추천이 완료되었습니다."),
    TEMPLATE_DAY_OPTIMIZE_SUCCESS(HttpStatus.OK, "최적 동선 정렬이 완료되었습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
