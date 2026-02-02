package org.umc.travlocksserver.domain.vlock.constant;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum VlockCategoryErrorCode implements BaseCode {

    DEFAULT_BLOCK_CATEGORY_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "기본 블록 카테고리가 존재하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
