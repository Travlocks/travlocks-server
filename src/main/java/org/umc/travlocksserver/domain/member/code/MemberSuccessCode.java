package org.umc.travlocksserver.domain.member.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseCode {

	EMAIL_EXISTS_CHECK_SUCCESS(HttpStatus.OK, "이메일 존재 여부 검사에 성공했습니다."),
	NICKNAME_EXISTS_CHECK_SUCCESS(HttpStatus.OK, "닉네임 중복 검사에 성공했습니다."),
	MEMBER_SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입이 완료되었습니다."),
	MEMBER_PROFILE_GET_SUCCESS(HttpStatus.OK, "유저 프로필 조회에 성공했습니다."),
	MEMBER_PASSWORD_UPDATED(HttpStatus.OK, "비밀번호가 변경되었습니다."),
	MEMBER_PROFILE_UPDATED(HttpStatus.OK, "프로필이 수정되었습니다."),
	FAVORITE_TEMPLATE_LIST_GET_SUCCESS(HttpStatus.OK, "내 즐겨찾기 목록 조회에 성공했습니다."),
	MEMBER_WITHDRAW_SUCCESS(HttpStatus.OK, "회원 탈퇴가 완료되었습니다."),
	MEMBER_MY_PAGE_RETRIEVED(HttpStatus.OK, "마이페이지 조회에 성공했습니다."),
	MEMBER_OAUTH_ONBOARDING_COMPLETED(HttpStatus.OK, "OAuth 온보딩이 완료되었습니다."),
	;

	private final HttpStatus status;
	private final String message;
}
