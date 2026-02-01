package org.umc.travlocksserver.domain.template.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationsDTO;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.global.response.SuccessResponse;

import java.util.List;

@Tag(name = "Template")
public interface TemplateControllerDocs {

    @Operation(
            summary = "AI 추천 템플릿 조회 API",
            description = "Rule-based 방식으로 추천된 템플릿을 조회하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI 템플릿 추천이 완료되었습니다.")
    })
    ResponseEntity<SuccessResponse<TemplateRecommendationsDTO>> getRecommendedTemplates(
            @AuthenticationPrincipal Long memberId
    );

    @Operation(
            summary = "홈 화면 인기 템플릿 조회",
            description = """
                홈 화면 하단에 노출되는 인기 템플릿 목록을 조회합니다.
                
                - 공개된 템플릿(isPublic = true)만 조회됩니다.
                - 리믹스 수(remixCount) 기준 내림차순으로 정렬됩니다.
                - 최대 10개의 템플릿을 반환합니다.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "홈 화면 인기 템플릿 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = SuccessResponse.class
                    )
            )
    )
    ResponseEntity<SuccessResponse<List<PopularTemplateResponse>>> getPopularTemplates();
}
