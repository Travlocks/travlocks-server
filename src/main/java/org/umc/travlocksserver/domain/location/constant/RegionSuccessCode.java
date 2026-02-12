package org.umc.travlocksserver.domain.location.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum RegionSuccessCode implements BaseCode {

    REGION_RETRIEVE_SUCCESS(HttpStatus.OK, "여행지 목록 조회에 성공했습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
