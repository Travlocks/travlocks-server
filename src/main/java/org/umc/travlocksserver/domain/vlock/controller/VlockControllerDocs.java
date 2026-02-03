package org.umc.travlocksserver.domain.vlock.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockResponseDTO;
import org.umc.travlocksserver.global.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

public interface VlockControllerDocs {

	@Operation(
		summary = "블록 생성",
		description = """
		템플릿에 사용될 블록을 생성합니다.
		
		• 요청에 포함된 카테고리와 도시는 필수입니다.
		• 모든 검증이 완료되면 블록 생성 요청이 접수됩니다.
		• 실제 저장은 비동기 처리되며, 요청 성공 시 202(ACCEPTED)를 반환합니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "202", description = "Vlock creation request accepted"),
		@ApiResponse(responseCode = "400", description = "Category Id or City Id is required"),
		@ApiResponse(responseCode = "404", description = "Category Id or City Id is invalid")
	})
	ResponseEntity<SuccessResponse<Void>> createVlock(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody VlockRequestDTO request);

	@Operation(
		summary = "인기 블록 조회",
		description = """
		특정 도시의 인기 블록 목록을 조회합니다.
		
		• 이용 횟수(usageCount) 기준 내림차순
		• 이용 횟수가 동일한 경우 최신 생성 순
		• 최대 20개의 블록을 반환합니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Popular vlocks retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "City Id is invalid")
	})
	ResponseEntity<SuccessResponse<List<VlockResponseDTO>>> getPopularVlocks(
		@PathVariable Long cityId);

	@Operation(
		summary = "카테고리 블록 조회",
		description = """
		특정 도시와 카테고리에 속한 블록 목록을 조회합니다.
		
		• 이용 횟수(usageCount) 기준 내림차순
		• 이용 횟수가 동일한 경우 최신 생성 순으로 정렬됩니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Category vlocks retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "City Id or Category Id is invalid")
	})
	ResponseEntity<SuccessResponse<List<VlockResponseDTO>>> getCategoriesVlocks(
		@PathVariable Long cityId,
		@PathVariable Long categoryId);

	@Operation(
		summary = "내가 생성한 블록 조회",
		description = """
		사용자가 생성한 블록 목록을 조회합니다.
		
		• 특정 도시 기준으로 조회됩니다.
		• 삭제되지 않은 블록만 반환합니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Created vlocks retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "City Id is invalid")
	})
	ResponseEntity<SuccessResponse<List<VlockResponseDTO>>> getMyVlocks(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long cityId);
}
