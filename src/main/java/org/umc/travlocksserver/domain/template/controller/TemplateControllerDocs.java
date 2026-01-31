package org.umc.travlocksserver.domain.template.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDetailResponseDTO;
import org.umc.travlocksserver.global.response.SuccessResponse;

import java.util.List;

@Tag(name = "Template")
public interface TemplateControllerDocs {

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

    @Operation(
            summary = "템플릿 상세 조회",
            description = """
                templateId에 해당하는 템플릿 상세 정보를 조회합니다.
                - 공개되지 않은 템플릿 조회 시 에러가 발생합니다.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "템플릿 상세 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = SuccessResponse.class
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "템플릿 조회 실패 (템플릿이 없거나 비공개)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = SuccessResponse.class
                    )
            )
    )
    ResponseEntity<SuccessResponse<TemplateDetailResponseDTO>> getTemplateDetail(Long templateId, @Parameter(hidden = true) Member member);
}
