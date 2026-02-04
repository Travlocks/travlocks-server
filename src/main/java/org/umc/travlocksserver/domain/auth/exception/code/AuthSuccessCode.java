package org.umc.travlocksserver.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseCode {

	EMAIL_VERIFICATION_CODE_SENT(HttpStatus.OK, "인증 코드가 발송되었습니다."),
	EMAIL_VERIFICATION_CONFIRMED(HttpStatus.OK, "이메일 인증이 완료되었습니다."),
	EMAIL_VERIFICATION_CODE_RESENT(HttpStatus.OK, "인증 코드가 재전송되었습니다."),
    AUTH_LOGIN_SUCCESS(HttpStatus.OK, "로그인에 성공했습니다."),
    AUTH_ACCESS_TOKEN_REISSUED(HttpStatus.OK, "액세스 토큰이 재발급되었습니다."),
    AUTH_LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃에 성공했습니다."),
    PASSWORD_RESET_LINK_SENT(HttpStatus.OK,  "비밀번호 재설정 링크가 전송되었습니다.")
    ;

	private final HttpStatus status;
	private final String message;
}
