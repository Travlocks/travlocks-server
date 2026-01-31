package org.umc.travlocksserver.domain.vlock.constant;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VlockErrorCode implements BaseCode {

	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
	VLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 블록입니다.")
	;

	private final HttpStatus status;
	private final String message;
}
