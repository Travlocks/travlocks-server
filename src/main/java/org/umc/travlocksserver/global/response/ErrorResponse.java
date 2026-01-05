package org.umc.travlocksserver.global.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.FieldError;
import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.code.ErrorCode;

public record ErrorResponse(
	String timeStamp,
	String errorCode,
	String errorMessage,
	Object details) {

	public ErrorResponse {
		if (timeStamp == null) {
			timeStamp = LocalDateTime.now().toString();
		}
	}

	public ErrorResponse(BaseCode errorCode) {
		this(
			LocalDateTime.now().toString(),
			errorCode.getCode(),
			errorCode.getMessage(),
			null);
	}

	public ErrorResponse(BaseCode errorCode, Object details) {
		this(
			LocalDateTime.now().toString(),
			errorCode.getCode(),
			errorCode.getMessage(),
			details);
	}

	public ErrorResponse(List<FieldError> fieldErrors) {
		this(
			LocalDateTime.now().toString(),
			ErrorCode.NOT_VALID_EXCEPTION.getCode(),
			ErrorCode.NOT_VALID_EXCEPTION.getMessage(),
			fieldErrors.stream()
				.collect(Collectors.toMap(
					FieldError::getField,
					fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값입니다.",
					(existing, replacement) -> existing)));
	}
}
