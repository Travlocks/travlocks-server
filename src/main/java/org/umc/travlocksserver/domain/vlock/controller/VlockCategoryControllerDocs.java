package org.umc.travlocksserver.domain.vlock.controller;

import org.springframework.http.ResponseEntity;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockCategoriesDTO;
import org.umc.travlocksserver.global.response.SuccessResponse;
import org.umc.travlocksserver.infra.redis.vlock.CachedVlockCategoryList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Vlock Category API", description = "블록 카테고리 관련 API 입니다.")
public interface VlockCategoryControllerDocs {

	@Operation(
		summary = "블록 카테고리 조회",
		description = """
		모든 카테고리 목록을 조회합니다.
		
		- 카테고리가 존재하지 않으면 서버 에러가 발생합니다.
		- 카테고리 목록을 반환합니다.
		"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Vlock category retrieved successfully"),
		@ApiResponse(responseCode = "500", description = "Default vlock category does not exist")
	})
	ResponseEntity<SuccessResponse<VlockCategoriesDTO>> getCategoriesVlocks();
}
