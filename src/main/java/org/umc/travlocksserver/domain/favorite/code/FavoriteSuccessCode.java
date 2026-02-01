package org.umc.travlocksserver.domain.favorite.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum FavoriteSuccessCode implements BaseCode {

    FAVORITE_ADD_SUCCESS(HttpStatus.OK, "즐겨찾기 추가 성공"),
    FAVORITE_REMOVE_SUCCESS(HttpStatus.OK, "즐겨찾기 취소 성공");

    private final HttpStatus status;
    private final String message;
}