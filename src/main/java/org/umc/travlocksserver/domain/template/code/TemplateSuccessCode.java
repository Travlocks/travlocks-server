package org.umc.travlocksserver.domain.template.code;

import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplateSuccessCode implements BaseCode {

	TEMPLATE_RECOMMEND_SUCCESS(HttpStatus.OK, "AI 템플릿 추천이 완료되었습니다."),
	HOME_GET_POPULAR_TEMPLATES_SUCCESS(HttpStatus.OK, "홈 화면 인기 템플릿 조회에 성공했습니다."),
	TEMPLATE_REMIX_SUCCESS(HttpStatus.CREATED, "템플릿 리믹스(복제)에 성공했습니다."),
	TEMPLATE_GET_CANVAS_SUCCESS(HttpStatus.OK, "템플릿 캔버스 조회에 성공했습니다."),
    TEMPLATE_DETAIL_GET_SUCCESS(HttpStatus.OK, "템플릿 상세 조회에 성공했습니다."),
	TEMPLATE_RECENT_GET_SUCCESS(HttpStatus.OK, "최근 편집 템플릿 조회에 성공했습니다.");

	private final HttpStatus status;
	private final String message;
}
