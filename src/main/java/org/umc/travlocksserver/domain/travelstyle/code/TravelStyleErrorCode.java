package org.umc.travlocksserver.domain.travelstyle.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum TravelStyleErrorCode implements BaseCode {

	TRAVEL_STYLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 여행 스타일 ID가 포함되어 있습니다."),
	TRAVEL_STYLE_MAX_EXCEEDED(HttpStatus.BAD_REQUEST, "여행 스타일은 최대 2개까지 선택할 수 있습니다."),
	INVALID_PREFERRED_STYLE_REQUEST(HttpStatus.BAD_REQUEST,
		"preferredTravelStyleIds는 null을 허용하지 않습니다. (필드 생략=유지, []=해제)"),
		;

	private final HttpStatus status;
	private final String message;
}
