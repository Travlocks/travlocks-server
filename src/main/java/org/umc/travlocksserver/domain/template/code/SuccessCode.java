package org.umc.travlocksserver.domain.template.code;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseCode {

    HOME_GET_POPULAR_TEMPLATES_SUCCESS(HttpStatus.OK, "홈 화면 인기 템플릿 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String message;
}
