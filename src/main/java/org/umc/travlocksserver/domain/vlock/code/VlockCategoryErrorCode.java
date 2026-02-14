package org.umc.travlocksserver.domain.vlock.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum VlockCategoryErrorCode implements BaseCode {

	DEFAULT_VLOCK_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "블록 카테고리가 존재하지 않습니다.");

	private final HttpStatus status;
	private final String message;
}
