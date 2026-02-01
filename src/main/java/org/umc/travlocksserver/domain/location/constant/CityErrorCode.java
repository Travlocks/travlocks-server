package org.umc.travlocksserver.domain.location.constant;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CityErrorCode implements BaseCode {

	CITY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 도시를 찾을 수 없습니다."),
	;

	private final HttpStatus status;
	private final String message;
}
