package org.umc.travlocksserver.domain.vlock.constant;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VlockSuccessCode implements BaseCode {

	VLOCK_CREATE_ACCEPTED(
		HttpStatus.ACCEPTED,
		"블록 생성 요청이 완료되었습니다."
	)
	;

	private final HttpStatus status;
	private final String message;
}
