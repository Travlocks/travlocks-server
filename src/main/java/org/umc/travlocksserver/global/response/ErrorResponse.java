package org.umc.travlocksserver.global.response;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.FieldError;
import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.code.ErrorCode;

public record ErrorResponse(
	boolean isSuccess,
	String errorCode,
	String errorMessage,
	Object data
) {

	public ErrorResponse(BaseCode errorCode) {
		this(
			false,
			errorCode.getCode(),
			errorCode.getMessage(),
			null);
	}

	public ErrorResponse(BaseCode errorCode, Object details) {
		this(
			false,
			errorCode.getCode(),
			errorCode.getMessage(),
			details);
	}

	public ErrorResponse(List<FieldError> fieldErrors) {
		this(
			false,
			ErrorCode.NOT_VALID_EXCEPTION.getCode(),
			ErrorCode.NOT_VALID_EXCEPTION.getMessage(),
			fieldErrors.stream()
				.collect(Collectors.toMap(
					FieldError::getField,
					fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값입니다.",
					(existing, replacement) -> existing)));
	}
}
