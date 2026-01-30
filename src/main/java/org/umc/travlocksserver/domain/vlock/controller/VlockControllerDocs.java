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
		summary = "블록 생성 API",
		description = """
		템플릿에 사용될 블록을 생성합니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "202", description = "Vlock creation request accepted."),
		@ApiResponse(responseCode = "400", description = "Category Id or City Id is required."),
		@ApiResponse(responseCode = "404", description = "Category Id, City Id is invalid.")
	})
	ResponseEntity<SuccessResponse<Void>> createVlock(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody VlockRequestDTO request);

	@Operation(
		summary = "생성 블록 조회 API",
		description = """
		사용자가 생성한 블록을 조회합니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Created vlock retrieved"),
		@ApiResponse(responseCode = "404", description = "City Id is invalid")
	})
	ResponseEntity<SuccessResponse<List<VlockResponseDTO>>> getMyVlock(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long cityId);
}
