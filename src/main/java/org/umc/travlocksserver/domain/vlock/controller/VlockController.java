package org.umc.travlocksserver.domain.vlock.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.vlock.code.VlockSuccessCode;
import org.umc.travlocksserver.domain.vlock.dto.request.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.request.VlockUpdateRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockResponseDTO;
import org.umc.travlocksserver.domain.vlock.service.command.VlockCommandService;
import org.umc.travlocksserver.domain.vlock.service.query.VlockQueryService;
import org.umc.travlocksserver.global.response.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vlocks")
public class VlockController implements VlockControllerDocs {

	private final VlockCommandService vlockCommandService;
	private final VlockQueryService vlockQueryService;

	/** 블록 생성 */
	@PostMapping
	public ResponseEntity<SuccessResponse<VlockResponseDTO>> createVlock(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody VlockRequestDTO request) {
		VlockResponseDTO response = vlockCommandService.createVlock(memberId, request);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(SuccessResponse.ok(VlockSuccessCode.VLOCK_CREATE_SUCCESS, response));
	}

	/** 인기 블록 조회 */
	@GetMapping("/cities/{cityId}/popular")
	public ResponseEntity<SuccessResponse<List<VlockResponseDTO>>> getPopularVlocks(
		@PathVariable Long cityId
	) {
		List<VlockResponseDTO> responses = vlockQueryService.getPopularVlocks(cityId);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(SuccessResponse.ok(VlockSuccessCode.VLOCK_GET_SUCCESS, responses));
	}

	/** 카테고리 블록 조회 */
	@GetMapping("/cities/{cityId}/categories/{categoryId}")
	public ResponseEntity<SuccessResponse<List<VlockResponseDTO>>> getCategoriesVlocks(
		@PathVariable Long cityId,
		@PathVariable Long categoryId
	) {
		List<VlockResponseDTO> responses = vlockQueryService.getCategoriesVlocks(cityId, categoryId);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(SuccessResponse.ok(VlockSuccessCode.VLOCK_GET_SUCCESS, responses));
	}

	/** 생성 블록 조회 */
	@GetMapping("/cities/{cityId}")
	public ResponseEntity<SuccessResponse<List<VlockResponseDTO>>> getMyVlocks(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long cityId
	) {
		List<VlockResponseDTO> responses = vlockQueryService.getMyVlocks(memberId, cityId);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(SuccessResponse.ok(VlockSuccessCode.VLOCK_GET_SUCCESS, responses));
	}

	/** 블록 수정 */
	@PutMapping("/{vlockId}")
	public ResponseEntity<SuccessResponse<VlockResponseDTO>> updateVlock(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long vlockId,
		@Valid @RequestBody VlockUpdateRequestDTO request
	) {
		VlockResponseDTO response = vlockCommandService.updateVlock(memberId, vlockId, request);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(SuccessResponse.ok(VlockSuccessCode.VLOCK_UPDATE_SUCCESS, response));
	}
}
