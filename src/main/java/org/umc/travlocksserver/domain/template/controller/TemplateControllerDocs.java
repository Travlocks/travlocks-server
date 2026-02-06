package org.umc.travlocksserver.domain.template.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.umc.travlocksserver.domain.template.dto.request.TemplatePreInputRequestDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplatePreInputResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.travlocksserver.domain.template.dto.request.TemplateRatingCreateRequestDTO;
import org.umc.travlocksserver.domain.template.dto.response.BatchRouteResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCanvasResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRemixResponseDTO;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.dto.response.*;
import org.umc.travlocksserver.global.response.ErrorResponse;
import org.umc.travlocksserver.global.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationsDTO;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDetailResponseDTO;

@Tag(name = "Template API", description = "템플릿 관련 API 입니다.")
public interface TemplateControllerDocs {

	@Operation(
		summary = "사전 정보 입력 API",
		description = """
			여행지, 기간, 교통수단, 테마 정보를 제출하면 캔버스 편집용 여정 작업물(초안)을 최초 생성합니다.
			
			- 기본 전체공개 (isPublic=true)
			- 공유 링크용 UUID(shareToken)는 생성 시점에 함께 발급
			"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "여정 작업물 초안 생성 성공"),
		@ApiResponse(responseCode = "400", description = "필수 데이터 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 cityId 또는 travelThemeId", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<SuccessResponse<TemplatePreInputResponseDTO>> createPreInput(
		@Valid @RequestBody TemplatePreInputRequestDTO request,
		@AuthenticationPrincipal Long memberId
	);

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
		summary = "템플릿 캔버스 조회 API",
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
	ResponseEntity<SuccessResponse<TemplateDetailResponseDTO>> getTemplateDetail(@PathVariable Long templateId,
		@Parameter(hidden = true) Member member);

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
		@PathVariable Long templateDayId,
		@PathVariable Integer dayNo
	);

	@Operation(
		summary = "템플릿 이동 루트 조회 API",
		description = """
			특정 템플릿의 N일차에 포함된 블록 간 이동 루트를 조회합니다.
	
			[Path Variable]
			- templateId: 조회할 템플릿 ID
			- dayNo: 조회할 일차 (1부터 시작)

			[Query Parameter]
			- transportType: 이동 수단 (WALK)			
        """
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "템플릿 이동 루트 조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 이동 수단", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 템플릿 또는 일차", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<SuccessResponse<BatchRouteResponseDTO>> getRoutes(
		@PathVariable Long templateId,
		@PathVariable Integer dayNo,
		@RequestParam TransportType transportType
	);

	@Operation(
			summary = "템플릿 탐색",
			description = """
                검색, 필터, 정렬 조건을 기준으로 템플릿을 조회합니다.
                - 검색(keyword)
                - 도시(cityNames) : 선택한 도시 필터링
                - 여행 테마(travelThemes) : 선택한 여행 테마 필터링
                - 여행 기간(tripDays) : 당일치기, 1박 2일, 2박 3일, 3박 4일, 4일 이상
                - 이동 수단(transportTypes) : 도보, 차량, 대중교통
                - 정렬(sort) : 최신순, 인기순, 별점순
                - 페이지(offset) : 0부터 시작하는 오프셋 기반 페이지
                """
	)
	@ApiResponse(
			responseCode = "200",
			description = "템플릿 탐색 성공",
			content = @Content(
					mediaType = "application/json",
					schema = @Schema(
							implementation = SuccessResponse.class
					)
			)
	)
	@ApiResponse(
			responseCode = "400",
			description = "템플릿 탐색 실패 (잘못된 요청)",
			content = @Content(
					mediaType = "application/json",
					schema = @Schema(
							implementation = ErrorResponse.class
					)
			)
	)
	ResponseEntity<SuccessResponse<List<TemplateExploreResponseDTO>>> exploreTemplates(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) List<String> cities,
			@RequestParam(required = false) List<String> themes,
			@RequestParam(required = false) List<String> tripDays,
			@RequestParam(required = false) List<String> transportTypes,
			@RequestParam(defaultValue = "별점순") String sort,
			@RequestParam(defaultValue = "0") int page
	);

	@Operation(
			summary = "최근 편집한 템플릿 조회",
			description = "로그인한 사용자가 최근에 편집한 템플릿 최신 2개를 조회합니다.",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "최근 편집한 템플릿 조회 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = TemplateLatestDTO.class)
							)
					),
					@ApiResponse(
							responseCode = "401",
							description = "로그인 필요"
					)
			}
	)
	ResponseEntity<SuccessResponse<List<TemplateLatestDTO>>> getRecentTemplates(
			@AuthenticationPrincipal Long memberId
	);

	@Operation(
			summary = "최적 동선 생성 API",
			description = "거리 기반으로 최적 동선을 생성하는 API 입니다."
	)
	@ApiResponse(
			responseCode = "200",
			description = "최적 동선 정렬이 완료되었습니다."
	)
	@ApiResponse(
			responseCode = "404",
			description = "존재하지 않는 템플릿 Day 입니다.",
			content = @Content(
					schema = @Schema(implementation = ErrorResponse.class)
			)
	)
	ResponseEntity<SuccessResponse<OptimizeResponseDTO>> optimize(
				@AuthenticationPrincipal Long memberId,
				@PathVariable Long templateId,
				@PathVariable Integer dayNo
		);

	@Operation(
		summary = "템플릿 평점 등록 API",
		description = """
			특정 템플릿에 평점을 등록합니다.
	
			[Path Variable]
			- templateId: 평점을 등록할 템플릿 ID
	
			[Request Body]
			- rating: 평점(1.0 ~ 5.0)
			- content: 리뷰 내용(선택)
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "템플릿 평점 등록 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패(rating 범위 오류 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 템플릿", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 등록된 평점", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<SuccessResponse<Void>> createTemplateRating(
		@PathVariable Long templateId,
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody TemplateRatingCreateRequestDTO request
	);
}
