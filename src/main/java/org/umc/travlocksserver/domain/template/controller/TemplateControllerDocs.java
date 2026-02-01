package org.umc.travlocksserver.domain.template.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCanvasResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRemixResponseDTO;
import org.umc.travlocksserver.global.response.ErrorResponse;
import org.umc.travlocksserver.global.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationsDTO;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDetailResponseDTO;

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
		summary = "템플릿 리믹스(복제) API",
		description = """
			기존 템플릿을 복제하여 새로운 템플릿을 생성합니다.
					
			[Path Variable]
			- templateId: 리믹스할 원본 템플릿 ID
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "템플릿 리믹스(복제) 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 템플릿", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<SuccessResponse<TemplateRemixResponseDTO>> remix(
		@PathVariable Long templateId,
		@AuthenticationPrincipal Long memberId
	);

	@Operation(
		summary = "템플릿 리믹스 캔버스 조회 API",
		description = """
			특정 템플릿의 N일차 캔버스를 조회합니다.
			캔버스에는 블록 목록이 포함됩니다.
				
			[Path Variable]
			- templateId: 조회할 템플릿 ID
			- dayNo: 조회할 일차(1부터 시작)
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "템플릿 캔버스 조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 템플릿 캔버스", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<SuccessResponse<TemplateCanvasResponseDTO>> getTemplateCanvas(
		@PathVariable Long templateId,
		@PathVariable Integer dayNo
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
                            implementation = ErrorResponse.class
                    )
            )
    )
    ResponseEntity<SuccessResponse<TemplateDetailResponseDTO>> getTemplateDetail(@PathVariable Long templateId, @Parameter(hidden = true) Member member);
}
