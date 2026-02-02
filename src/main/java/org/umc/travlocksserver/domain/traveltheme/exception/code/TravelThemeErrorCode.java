package org.umc.travlocksserver.domain.traveltheme.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum TravelThemeErrorCode implements BaseCode {

    TRAVEL_THEME_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 여행 테마 ID가 포함되어 있습니다."),
    TRAVEL_THEME_MAX_EXCEEDED(HttpStatus.BAD_REQUEST, "여행 테마는 최대 2개까지 선택할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
