package org.umc.travlocksserver.domain.template.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRemixResponseDTO;
import org.umc.travlocksserver.global.response.ErrorResponse;
import org.umc.travlocksserver.global.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface TemplateControllerDocs {

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
}
