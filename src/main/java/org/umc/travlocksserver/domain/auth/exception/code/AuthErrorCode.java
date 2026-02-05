package org.umc.travlocksserver.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseCode {

    EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "인증 요청이 만료되었거나 존재하지 않습니다."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않습니다."),

    INVALID_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 회원가입 토큰입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "리프레시 토큰이 필요합니다."),
    PASSWORD_RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "비밀번호 재설정 토큰이 올바르지 않거나 만료되었습니다."),
    PASSWORD_RESET_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    PASSWORD_RESET_WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호는 최소 8자 이상이며 영문과 숫자를 포함해야 합니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
