package org.umc.travlocksserver.domain.notification.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.umc.travlocksserver.domain.notification.constant.NotificationSuccessCode;
import org.umc.travlocksserver.domain.notification.service.command.NotificationCommandService;
import org.umc.travlocksserver.global.response.SuccessResponse;
import org.umc.travlocksserver.global.security.cookie.CookieFactory;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final CookieFactory cookieFactory;

    private final NotificationCommandService notificationCommandService;


    @PostMapping("/sse-token")
    public ResponseEntity<SuccessResponse<Void>> issueSseToken(
            @AuthenticationPrincipal Long memberId,
            HttpServletResponse response
    ) {
        String sseToken = notificationCommandService.generateSseToken(memberId);
        ResponseCookie cookie = cookieFactory.createSseTokenCookie(sseToken);

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity
                .ok(SuccessResponse.ok(NotificationSuccessCode.SSE_TOKEN_ISSUED));
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@AuthenticationPrincipal Long memberId){
        return notificationCommandService.subscribe(memberId);
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<Void>> deleteAllNotifications(
            @AuthenticationPrincipal Long memberId
    ){
        notificationCommandService.deleteAllNotification(memberId);
        return ResponseEntity
                .ok(SuccessResponse.ok(NotificationSuccessCode.NOTIFICATION_DELETED_SUCCESS));
    }
}
