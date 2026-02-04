package org.umc.travlocksserver.domain.vlock.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.umc.travlocksserver.domain.vlock.dto.request.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.request.VlockUpdateRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockResponseDTO;
import org.umc.travlocksserver.global.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Vlock API", description = "블록 관련 API 입니다.")
public interface VlockControllerDocs {

	@Operation(
		summary = "블록 생성",
		description = """
		템플릿에 사용될 블록을 생성합니다.
		
		- 요청에 포함된 카테고리와 도시는 필수입니다.
		- 모든 검증이 완료되면 블록이 생성됩니다.
		- 생성된 블록을 반환합니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Vlock creat successfully"),
		@ApiResponse(responseCode = "400", description = "Category Id or City Id is required"),
		@ApiResponse(responseCode = "404", description = "Category Id or City Id is invalid")
	})
	ResponseEntity<SuccessResponse<VlockResponseDTO>> createVlock(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody VlockRequestDTO request);

	@Operation(
		summary = "인기 블록 조회",
		description = """
		특정 도시의 인기 블록 목록을 조회합니다.
		
		- 이용 횟수(usageCount) 기준 내림차순
		- 이용 횟수가 동일한 경우 최신 생성 순
		- 최대 20개의 블록을 반환합니다.
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
		
		- 이용 횟수(usageCount) 기준 내림차순
		- 이용 횟수가 동일한 경우 최신 생성 순으로 정렬됩니다.
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
		
		- 특정 도시 기준으로 조회됩니다.
		- 삭제되지 않은 블록만 반환합니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Created vlocks retrieved successfully"),
		@ApiResponse(responseCode = "404", description = "City Id is invalid")
	})
	ResponseEntity<SuccessResponse<List<VlockResponseDTO>>> getMyVlocks(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long cityId);

	@Operation(
		summary = "블록 수정",
		description = """
		사용자가 생성한 블록의 정보를 수정합니다.
		
		- 수정 가능 항목 : 블록 이름, 주소, 카테고리, 도시, 메모, URL, 공개/비공개 여부
		- 본인이 생성한 블록만 수정할 수 있습니다.
		- 삭제된 블록(deletedAt != null)은 수정할 수 없습니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Vlock updated successfully"),
		@ApiResponse(responseCode = "400", description = "Name, Address, Category ID, City ID is required"),
		@ApiResponse(responseCode = "403", description = "Access to the specified vlock is forbidden"),
		@ApiResponse(responseCode = "404", description = "Vlock ID, Category ID, City ID is invalid")
	})
	ResponseEntity<SuccessResponse<VlockResponseDTO>> updateVlock(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long vlockId,
		@Valid @RequestBody VlockUpdateRequestDTO request
	);

	@Operation(
		summary = "블록 삭제",
		description = """
		사용자가 생성한 블록을 삭제합니다.
		
		- 본인이 생성한 블록만 삭제할 수 있습니다.
		- 이미 삭제된 블록은 다시 삭제할 수 없습니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Vlock deleted successfully"),
		@ApiResponse(responseCode = "400", description = "Vlock is already deleted"),
		@ApiResponse(responseCode = "403", description = "Access to the specified vlock is forbidden"),
		@ApiResponse(responseCode = "404", description = "Vlock ID is invalid")
	})
	ResponseEntity<SuccessResponse<Void>> deleteVlock(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long vlockId
	);
}
