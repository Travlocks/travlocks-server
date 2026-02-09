package org.umc.travlocksserver.domain.vlock.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VlockErrorCode implements BaseCode {

	VLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 블록입니다."),
	START_VLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 출발 블록입니다."),
	END_VLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 도착 블록입니다."),

	VLOCK_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 블록에 대한 접근 권한이 없습니다."),

	VLOCK_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 블록입니다.")
	;

	private final HttpStatus status;
	private final String message;
}
