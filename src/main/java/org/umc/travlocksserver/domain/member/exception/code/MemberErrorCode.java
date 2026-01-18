package org.umc.travlocksserver.domain.member.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseCode {

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    SIGNUP_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 signupToken입니다."),
    SIGNUP_TOKEN_EMAIL_MISMATCH(HttpStatus.UNAUTHORIZED, "signupToken의 이메일 정보가 일치하지 않습니다."),

    POLICY_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 약관이 포함되어 있습니다."),
    REQUIRED_POLICY_NOT_AGREED(HttpStatus.BAD_REQUEST, "필수 약관에 동의해야 합니다."),

    TRAVEL_STYLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 여행 스타일 ID가 포함되어 있습니다."),
    TRAVEL_THEME_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 여행 테마 ID가 포함되어 있습니다.");

    private final HttpStatus status;
    private final String message;
}

