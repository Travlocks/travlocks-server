package org.umc.travlocksserver.domain.member.exception.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseCode {

	EMAIL_EXISTS_CHECK_SUCCESS(
		HttpStatus.OK,
		"이메일 중복 검사에 성공했습니다."
	),
	NICKNAME_EXISTS_CHECK_SUCCESS(
		HttpStatus.OK,
		"닉네임 중복 검사에 성공했습니다."
	),
	MEMBER_SIGNUP_SUCCESS(
		HttpStatus.CREATED,
		"회원가입이 완료되었습니다."
	),
	MEMBER_PROFILE_GET_SUCCESS(
		HttpStatus.OK,
		"유저 프로필 조회에 성공했습니다."
	);

	private final HttpStatus status;
	private final String message;
}
