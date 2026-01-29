package org.umc.travlocksserver.domain.template.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.template.code.TemplateSuccessCode;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRemixResponseDTO;
import org.umc.travlocksserver.domain.template.service.command.TemplateRemixService;
import org.umc.travlocksserver.global.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates")
public class TemplateController implements TemplateControllerDocs {

	private final TemplateRemixService templateRemixService;

	@PostMapping("/{templateId}/remix")
	public ResponseEntity<SuccessResponse<TemplateRemixResponseDTO>> remix(
		@PathVariable Long templateId,
		@AuthenticationPrincipal Long memberId
	) {
		TemplateSuccessCode successCode = TemplateSuccessCode.TEMPLATE_REMIX_SUCCESS;

		TemplateRemixResponseDTO data = templateRemixService.remix(templateId, memberId);

		return ResponseEntity
			.status(successCode.getStatus())
			.body(SuccessResponse.ok(successCode, data));
	}
}
