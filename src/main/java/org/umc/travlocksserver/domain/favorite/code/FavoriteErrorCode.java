package org.umc.travlocksserver.domain.favorite.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum FavoriteErrorCode implements BaseCode {

	ALREADY_FAVORITED(HttpStatus.CONFLICT, "이미 즐겨찾기한 템플릿입니다."),
	FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "즐겨찾기하지 않은 템플릿입니다."),
	FAVORITE_COUNT_BELOW_ZERO(HttpStatus.BAD_REQUEST, "즐겨찾기 수는 0보다 작을 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
