package org.umc.travlocksserver.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.apiPayload.code.BaseCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseCode {

    EMAIL_ALREADY_REGISTERED(
            HttpStatus.BAD_REQUEST,
            "이미 가입된 이메일입니다."
    );

    private final HttpStatus status;
    private final String message;
}
