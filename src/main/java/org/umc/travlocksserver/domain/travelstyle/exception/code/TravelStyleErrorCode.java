package org.umc.travlocksserver.domain.travelstyle.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum TravelStyleErrorCode implements BaseCode {

    TRAVEL_STYLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 여행 스타일 ID가 포함되어 있습니다."),
    TRAVEL_STYLE_MAX_EXCEEDED(HttpStatus.BAD_REQUEST, "여행 스타일은 최대 2개까지 선택할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
