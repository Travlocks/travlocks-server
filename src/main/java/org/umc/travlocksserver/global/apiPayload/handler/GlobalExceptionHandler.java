package org.umc.travlocksserver.global.apiPayload.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.umc.travlocksserver.global.apiPayload.ApiResponse;
import org.umc.travlocksserver.global.apiPayload.code.BaseCode;
import org.umc.travlocksserver.global.apiPayload.code.common.CommonErrorCode;
import org.umc.travlocksserver.global.apiPayload.exception.GeneralException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// ✨ 애플리케이션 전체에서 발생하는 예외를 catch 해서 통일된 응답(JSON)으로 반환하는 역할을 하는 클래스
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ⚪ GeneralException 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        BaseCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    // ⚪ 요청 헤더 누락 예외 처리
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException e) {
        BaseCode errorCode = CommonErrorCode.REQUEST_HEADER_EMPTY;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    // ⚪ 지원하지 않는 HTTP 메서드 예외 처리
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(Exception e) {
        BaseCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    // ⚪ 잘못된 URL 접근 예외 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException e) {
        BaseCode errorCode = CommonErrorCode.NOT_FOUND_URL;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    // ⚪ DTO Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        BaseCode commonErrorCode = CommonErrorCode.NOT_VALID_EXCEPTION;
        /*
        e.getBindingResult().getFieldErrors(): DTO 검증 중 실패한 필드들의 정보를 모두 가져옴
        FieldError 객체
        - field: 실패한 필드 이름
        - defaultMessage: 어노테이션에서 설정한 오류메시지
        */
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Optional.ofNullable(error.getDefaultMessage()).orElse("유효하지 않은 값입니다."), // default message가 null일 경우 기본 문자열로 대체
                        (existing, replacement) -> existing  // 같은 필드 이름이 나오면, 기존값은 두고 새값은 무시한다.
                ));
        return ResponseEntity.status(commonErrorCode.getStatus())
                .body(ApiResponse.fail(commonErrorCode, errors));
    }

    // ⚪ Media Type 오류 예외 처리
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e) {
        BaseCode commonErrorCode = CommonErrorCode.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE;
        return ResponseEntity.status(commonErrorCode.getStatus())
                .body(ApiResponse.fail(commonErrorCode));
    }

    // ⚪ 그 외의 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception e) {
        log.error("예상치 못한 예외가 발생했습니다.", e);
        BaseCode commonErrorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(commonErrorCode.getStatus())
                .body(ApiResponse.fail(commonErrorCode));
    }
}
