package org.umc.travlocksserver.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.apiPayload.code.BaseCode;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseCode {

    EMAIL_VERIFICATION_CODE_SENT(
            HttpStatus.OK,
            "인증 코드가 발송되었습니다."
    ),
    EMAIL_AVAILABLE(
            HttpStatus.OK,
            "사용 가능한 이메일입니다."
    ),
    EMAIL_ALREADY_EXISTS(
            HttpStatus.OK,
            "이미 가입된 이메일입니다."
    ),
    EMAIL_VERIFICATION_CONFIRMED(
            HttpStatus.OK,
            "이메일 인증이 완료되었습니다."
    ),

    ;

    private final HttpStatus status;
    private final String message;
}

