package org.umc.travlocksserver.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.code.ErrorCode;
import org.umc.travlocksserver.global.response.ErrorResponse;

// ✨ 애플리케이션 전체에서 발생하는 예외를 catch 해서 통일된 응답(JSON)으로 반환하는 역할을 하는 클래스
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// ⚪ GeneralException 처리
	@ExceptionHandler(GeneralException.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(GeneralException e) {
		BaseCode errorCode = e.getErrorCode();

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(new ErrorResponse(errorCode));
	}

	// ⚪ 요청 헤더 누락 예외 처리
	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ErrorResponse> handleMissingHeader() {
		return convert(ErrorCode.REQUEST_HEADER_EMPTY);
	}

	// ⚪ 지원하지 않는 HTTP 메서드 예외 처리
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotAllowed() {
		return convert(ErrorCode.METHOD_NOT_ALLOWED);
	}

	// ⚪ 요청 파라미터 타입 불일치 예외 처리
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch() {
		return convert(ErrorCode.INVALID_VALUE);
	}

	// ⚪ 잘못된 URL 접근 예외 처리
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound() {
		return convert(ErrorCode.NOT_FOUND_URL);
	}

	// ⚪ Media Type 오류 예외 처리
	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable() {
		return convert(ErrorCode.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE);
	}

	// ⚪ 그 외의 예외 처리
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnhandledException(Exception e) {
		log.error("예상치 못한 예외가 발생했습니다.", e);
		return convert(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	// ⚪ DTO Validation 예외 처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
		ErrorResponse errorResponse = new ErrorResponse(e.getBindingResult().getFieldErrors());

		return ResponseEntity
			.status(ErrorCode.NOT_VALID_EXCEPTION.getStatus())
			.body(errorResponse);
	}

	private ResponseEntity<ErrorResponse> convert(BaseCode errorCode) {
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(new ErrorResponse(errorCode));
	}
}
