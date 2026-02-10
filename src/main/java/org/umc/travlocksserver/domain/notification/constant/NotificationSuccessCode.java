package org.umc.travlocksserver.domain.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
@RequiredArgsConstructor
public enum NotificationSuccessCode implements BaseCode {

    SSE_TOKEN_ISSUED(HttpStatus.OK, "SSE 토큰이 발급되었습니다."),
    NOTIFICATION_DELETED_SUCCESS(HttpStatus.OK, "알림이 삭제되었습니다."),
    NOTIFICATION_GET_SUCCESS(HttpStatus.OK, "알림이 조회되었습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
