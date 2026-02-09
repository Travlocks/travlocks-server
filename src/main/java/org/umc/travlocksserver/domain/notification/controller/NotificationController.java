package org.umc.travlocksserver.domain.notification.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.umc.travlocksserver.domain.notification.constant.NotificationSuccessCode;
import org.umc.travlocksserver.domain.notification.service.command.NotificationCommandService;
import org.umc.travlocksserver.global.jwt.JwtTokenProvider;
import org.umc.travlocksserver.global.response.SuccessResponse;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final JwtTokenProvider jwtTokenProvider;

    private final NotificationCommandService notificationCommandService;

    @Value("${sse.token-ttl-ms}")
    private long tokenTtlMs;

    @PostMapping("/sse-token")
    public ResponseEntity<SuccessResponse<Void>> issueSseToken(
            @AuthenticationPrincipal Long memberId,
            HttpServletResponse response
    ) {
        long ttlSeconds = tokenTtlMs / 1000L;
        String sseToken = jwtTokenProvider.generateSseToken(memberId, ttlSeconds);

        ResponseCookie cookie = ResponseCookie.from("SSE_TOKEN", sseToken)
                .httpOnly(true)
                .secure(true)  // 로컬 http면 false, 운영 https면 true
                .sameSite("None")
                .path("/api/v1/notifications")
                .maxAge(Duration.ofSeconds(ttlSeconds))
                .build();

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
