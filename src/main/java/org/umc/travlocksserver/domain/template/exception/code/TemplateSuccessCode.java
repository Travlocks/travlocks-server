package org.umc.travlocksserver.domain.template.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@AllArgsConstructor
@Getter
public enum TemplateSuccessCode implements BaseCode {
    TEMPLATE_RECOMMEND_SUCCESS(
            HttpStatus.OK,
            "AI 템플릿 추천이 완료되었습니다."
    )
    ;

    private final HttpStatus status;
    private final String message;
}
