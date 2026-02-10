package org.umc.travlocksserver.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.umc.travlocksserver.domain.notification.dto.response.NotificationAllResponseDTO;
import org.umc.travlocksserver.global.response.SuccessResponse;

@Tag(name = "알림 API", description = "알림 관련 API 입니다.")
public interface NotificationControllerDocs {

    @Operation(
            summary = "SSE 토큰 발급 및 쿠키 저장 API",
            description = """
                    SSE 토큰 발급 후 쿠키에 저장하는 API입니다.
                    - SSE 연결 시, 브라우저의 EventSource API는 Authorization Header을 지원하지 않습니다.
                    - 따라서 해당 API를 통해 SSE 인증용 토큰을 발급받고 쿠키에 저장한 뒤,
                    SSE 연결 요청 시 브라우저가 자동으로 해당 쿠키를 포함하여 전송하도록 합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "SSE 토큰이 발급되었습니다.")
    public ResponseEntity<SuccessResponse<Void>> issueSseToken(
            @AuthenticationPrincipal Long memberId,
            HttpServletResponse response
    );

    @Operation(
            summary = "SSE 알림 구독 연결 API",
            description = """
                    서버-클라이언트 간 실시간 알림 전송을 위한 SSE 연결 API입니다.
                    - "/api/v1/notifications/sse-token" API를 통해 발급받은 쿠키(SSE_TOKEN)가 있어야 인증이 완료됩니다.
                    """
    )
    public SseEmitter subscribe(@AuthenticationPrincipal Long memberId);

    @Operation(
            summary = "알림 전체 삭제 API",
            description = """
                    알림 전체 삭제 API입니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "알림이 삭제되었습니다.")
    public ResponseEntity<SuccessResponse<Void>> deleteAllNotifications(
            @AuthenticationPrincipal Long memberId
    );

    @Operation(
            summary = "알림 리스트 조회 API",
            description = """
                    알림 리스트 조회 API입니다.
                    - 로그인한 회원의 알림 내역을 최신순으로 조회합니다.
                    - 무한 스크롤을 위한 커서 기반 페이지네이션을 지원합니다.
                    
                    [파라미터]
                    - cursor: 이전에 받았던 마지막 알림의 식별자입니다.
                      첫 페이지 조회 시에는 비우고 요청하며, 다음 요청부터는 이전 요청의 응답값 중 nextCursor을 넣어 요청합니다.
                    - size: 한 번에 조회할 알림의 개수입니다. (기본값: 10)
                    
                    [응답]
                    - hasNext: 다음 페이지가 있는지를 true/false로 반환합니다.
                    - nextCursor: 다음 요청에서 파라미터 중 cursor에 넣을 값을 반환합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "알림이 조회되었습니다.")
    public ResponseEntity<SuccessResponse<NotificationAllResponseDTO>> getNotifications(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") Integer size
    );
}
