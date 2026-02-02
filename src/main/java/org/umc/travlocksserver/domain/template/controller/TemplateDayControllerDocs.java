package org.umc.travlocksserver.domain.template.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.umc.travlocksserver.domain.template.dto.response.VlockSuggestionsResponseDTO;
import org.umc.travlocksserver.global.response.ErrorResponse;
import org.umc.travlocksserver.global.response.SuccessResponse;

@Tag(name = "템플릿 Day API", description = "템플릿 Day 관련 API 입니다.")
public interface TemplateDayControllerDocs {

    @Operation(
            summary = "AI 블록 추천 조회 API",
            description = "AI 기반으로 추천된 블록을 조회하는 API 입니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "AI 블록 추천이 완료되었습니다."
    )
    @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 템플릿 Day 입니다.",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "503",
            description = "AI 연동에 실패했습니다.",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "기본 블록 카테고리가 존재하지 않습니다.",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    ResponseEntity<SuccessResponse<VlockSuggestionsResponseDTO>> suggestions(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long templateDayId
    );
}
