package org.umc.travlocksserver.domain.vlock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockResponseDTO;
import org.umc.travlocksserver.domain.vlock.service.VlockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vlocks")
public class VlockController {

	private final VlockService vlockService;

	@PostMapping
	public ResponseEntity<VlockResponseDTO> createVlock(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody VlockRequestDTO request) {
		VlockResponseDTO response = vlockService.createVlock(memberId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
