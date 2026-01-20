package org.umc.travlocksserver.global.response;

import org.umc.travlocksserver.global.code.BaseCode;

public record SuccessResponse<T>(
	boolean isSuccess,
	String successCode,
	String successMessage,
	T data) {

	public static <T> SuccessResponse<T> ok(BaseCode successCode) {
		return new SuccessResponse<>(
			true,
			successCode.getCode(),
			successCode.getMessage(),
			null);
	}

	public static <T> SuccessResponse<T> ok(BaseCode successCode, T data) {
		return new SuccessResponse<>(
			true,
			successCode.getCode(),
			successCode.getMessage(),
			data);
	}
}
