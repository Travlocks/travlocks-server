package org.umc.travlocksserver.domain.member.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.apiPayload.code.BaseCode;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseCode {

    EMAIL_AVAILABLE(
            HttpStatus.OK,
            "사용 가능한 이메일입니다."
    ),
    EMAIL_ALREADY_EXISTS(
            HttpStatus.OK,
            "이미 가입된 이메일입니다."
    ),
    NICKNAME_AVAILABLE(
            HttpStatus.OK,
            "사용 가능한 닉네임입니다."
    ),
    NICKNAME_ALREADY_EXISTS(
            HttpStatus.OK,
            "이미 사용 중인 닉네임입니다."
    );
    ;

    private final HttpStatus status;
    private final String message;
}
