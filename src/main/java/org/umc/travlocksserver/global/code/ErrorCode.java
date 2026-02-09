package org.umc.travlocksserver.global.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseCode {

	// 400
	REQUEST_HEADER_EMPTY(HttpStatus.BAD_REQUEST, "요청 헤더가 비어있습니다."),
	NOT_VALID_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
	INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문이 올바르지 않습니다."),
	INVALID_VALUE(HttpStatus.BAD_REQUEST, "값이 비어있거나 유효하지 않습니다."),

	// 404
	NOT_FOUND_URL(HttpStatus.NOT_FOUND, "존재하지 않는 URL 입니다."),

	// 405
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),

	//406
	HTTP_MEDIA_TYPE_NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "요청한 미디어 타입을 제공할 수 없습니다."),

	// 500
	S3_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드에 실패했습니다."),
	FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 읽기 중 오류가 발생했습니다."),
	S3_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 삭제에 실패했습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	AI_SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답에 오류가 발생했습니다."),
	AI_RESPONSE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답이 비어있습니다.")
	;

	private final HttpStatus status;
	private final String message;
}
