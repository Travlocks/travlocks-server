package org.umc.travlocksserver.domain.vlock.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VlockSuccessCode implements BaseCode {

	VLOCK_CREATE_SUCCESS(HttpStatus.CREATED, "블록 생성이 완료되었습니다."),

	VLOCK_GET_SUCCESS(HttpStatus.OK, "블록을 성공적으로 조회했습니다.")
	;

	private final HttpStatus status;
	private final String message;
}
