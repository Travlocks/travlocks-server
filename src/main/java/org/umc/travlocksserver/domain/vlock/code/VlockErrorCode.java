package org.umc.travlocksserver.domain.vlock.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VlockErrorCode implements BaseCode {

	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
	VLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 블록입니다."),
	START_VLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 출발 블록입니다."),
	END_VLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 도착 블록입니다.")
	;

	private final HttpStatus status;
	private final String message;
}
