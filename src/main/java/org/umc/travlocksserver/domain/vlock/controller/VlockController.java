package org.umc.travlocksserver.domain.vlock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.vlock.constant.VlockSuccessCode;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.service.VlockService;
import org.umc.travlocksserver.global.response.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vlocks")
public class VlockController implements VlockControllerDocs {

	private final VlockService vlockService;

	@PostMapping
	public ResponseEntity<SuccessResponse<Void>> createVlock(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody VlockRequestDTO request) {
		vlockService.createVlock(memberId, request);

		return ResponseEntity
			.status(HttpStatus.ACCEPTED)
			.body(SuccessResponse.ok(VlockSuccessCode.VLOCK_CREATE_ACCEPTED));
	}
}
