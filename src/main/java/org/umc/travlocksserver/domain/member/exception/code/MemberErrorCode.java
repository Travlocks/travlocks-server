package org.umc.travlocksserver.domain.member.exception.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseCode {

	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 2~10자의 한글 또는 영문만 사용할 수 있습니다."),

    SIGNUP_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 signupToken입니다."),
	SIGNUP_TOKEN_EMAIL_MISMATCH(HttpStatus.UNAUTHORIZED, "signupToken의 이메일 정보가 일치하지 않습니다."),

	POLICY_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 약관이 포함되어 있습니다."),
	REQUIRED_POLICY_NOT_AGREED(HttpStatus.BAD_REQUEST, "필수 약관에 동의해야 합니다."),

	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    SAME_AS_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호가 현재 비밀번호와 동일합니다."),
    MEMBER_DELETED(HttpStatus.UNAUTHORIZED, "탈퇴한 회원입니다."),
    INVALID_ONBOARDING_STATUS(HttpStatus.BAD_REQUEST, "온보딩 상태의 회원만 완료할 수 있습니다."),
    ;

	private final HttpStatus status;
	private final String message;
}

