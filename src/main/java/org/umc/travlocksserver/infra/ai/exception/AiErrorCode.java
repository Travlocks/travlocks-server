package org.umc.travlocksserver.infra.ai.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements BaseCode {

    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 AI 서버에 오류가 발생했습니다."),
    AI_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 외부 AI 요청입니다."),
    AI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "외부 AI 응답 시간이 초과되었습니다."),
    AI_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 가공 중 오류가 발생했습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
