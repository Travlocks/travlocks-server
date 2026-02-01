package org.umc.travlocksserver.domain.template.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.template.code.TemplateSuccessCode;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCanvasResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRemixResponseDTO;
import org.umc.travlocksserver.domain.template.service.command.TemplateRemixService;
import org.umc.travlocksserver.domain.template.service.query.TemplateCanvasQueryService;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationsDTO;
import org.umc.travlocksserver.domain.template.service.query.TemplateQueryService;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDetailResponseDTO;
import org.umc.travlocksserver.global.annotation.LoginUser;
import org.umc.travlocksserver.global.response.SuccessResponse;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates")
public class TemplateController implements TemplateControllerDocs {

    private final TemplateQueryService templateQueryService;
	private final TemplateRemixService templateRemixService;
	private final TemplateCanvasQueryService templateCanvasQueryService;

    @GetMapping("/recommendations")
    public ResponseEntity<SuccessResponse<TemplateRecommendationsDTO>> getRecommendedTemplates(
            @AuthenticationPrincipal Long memberId
    ) {
        TemplateRecommendationsDTO response = templateQueryService.getRecommendedTemplates(memberId);
        return ResponseEntity.ok(SuccessResponse.ok(TemplateSuccessCode.TEMPLATE_RECOMMEND_SUCCESS, response));
    }

    @GetMapping("/popular")
    public ResponseEntity<SuccessResponse<List<PopularTemplateResponse>>> getPopularTemplates() {
        TemplateSuccessCode successCode = TemplateSuccessCode.HOME_GET_POPULAR_TEMPLATES_SUCCESS;
        return ResponseEntity
                .status(successCode.getStatus())
                .body(
                        SuccessResponse.ok(
                                successCode,
                                templateQueryService.getPopularTemplates(10)
                        )
                );
    }

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

	@GetMapping("/{templateId}/days/{dayNo}/canvas")
	public ResponseEntity<SuccessResponse<TemplateCanvasResponseDTO>> getTemplateCanvas(
		@PathVariable Long templateId,
		@PathVariable Integer dayNo
	) {
		TemplateSuccessCode successCode = TemplateSuccessCode.TEMPLATE_GET_CANVAS_SUCCESS;

		TemplateCanvasResponseDTO data = templateCanvasQueryService.getTemplateCanvas(templateId, dayNo);

		return ResponseEntity
			.status(successCode.getStatus())
			.body(SuccessResponse.ok(successCode, data));
	}

    @GetMapping("/{templateId}")
    @Override
    public ResponseEntity<SuccessResponse<TemplateDetailResponseDTO>> getTemplateDetail(@PathVariable Long templateId, @Parameter(hidden = true) @LoginUser Member member) {
        Long memberId = (member != null) ? member.getId() : null;

        TemplateDetailResponseDTO dto =
                templateQueryService.getTemplateDetail(templateId, memberId);

        return ResponseEntity.ok(
                SuccessResponse.ok(
                        TemplateSuccessCode.TEMPLATE_DETAIL_GET_SUCCESS,
                        dto
                )
        );
    }
}
