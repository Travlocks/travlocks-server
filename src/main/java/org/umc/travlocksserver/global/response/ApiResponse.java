package org.umc.travlocksserver.global.response;

import org.umc.travlocksserver.global.code.BaseCode;

// ✨ 모든 API 응답을 일관된 JSON 구조로 반환하기 위한 레코드
public record ApiResponse<T>(
	boolean isSuccess,
	String code,
	String message,
	T data) {

	// ⚪ 성공 응답 (데이터 포함)
	public static <T> ApiResponse<T> ok(BaseCode code, T data) {
		return new ApiResponse<>(true, code.getCode(), code.getMessage(), data);
	}

	// ⚪ 성공 응답
	public static <T> ApiResponse<T> ok(BaseCode code) {
		return new ApiResponse<>(true, code.getCode(), code.getMessage(), null);
	}

	// ⚪ 실패 응답 (데이터 포함)
	public static <T> ApiResponse<T> fail(BaseCode code, T data) {
		return new ApiResponse<>(false, code.getCode(), code.getMessage(), data);
	}

	// ⚪ 실패 응답
	public static <T> ApiResponse<T> fail(BaseCode code) {
		return new ApiResponse<>(false, code.getCode(), code.getMessage(), null);
	}
}
