package org.umc.travlocksserver.domain.vlock.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VlockCategorySuccessCode implements BaseCode {

	DEFAULT_VLOCK_CATEGORY_GET_SUCCESS(HttpStatus.OK, "기본 블록 카테고리를 성공적으로 조회했습니다.")
	;

	private final HttpStatus status;
	private final String message;
}
