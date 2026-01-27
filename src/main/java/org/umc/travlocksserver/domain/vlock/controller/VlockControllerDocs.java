package org.umc.travlocksserver.domain.vlock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockResponseDTO;

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
		@ApiResponse(responseCode = "201", description = "블록 생성 성공"),
		@ApiResponse(responseCode = "404", description = "categoryId or cityId가 누락된 경우 실패")
	})
	ResponseEntity<VlockResponseDTO> createVlock(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody VlockRequestDTO request);
}
