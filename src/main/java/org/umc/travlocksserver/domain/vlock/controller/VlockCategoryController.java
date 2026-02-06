package org.umc.travlocksserver.domain.vlock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.vlock.code.VlockCategorySuccessCode;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockCategoriesDTO;
import org.umc.travlocksserver.domain.vlock.service.query.VlockCategoryQueryService;
import org.umc.travlocksserver.global.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vlocks/categories")
public class VlockCategoryController {

	private final VlockCategoryQueryService vlockCategoryQueryService;

	@GetMapping
	public ResponseEntity<SuccessResponse<VlockCategoriesDTO>> getCategoriesVlocks() {
		VlockCategoriesDTO responses = vlockCategoryQueryService.getAllCategories();

		return ResponseEntity.
			status(HttpStatus.OK)
			.body(SuccessResponse.ok(VlockCategorySuccessCode.DEFAULT_VLOCK_CATEGORY_GET_SUCCESS, responses));
	}
}
