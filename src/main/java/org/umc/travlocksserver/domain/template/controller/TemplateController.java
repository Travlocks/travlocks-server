package org.umc.travlocksserver.domain.template.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationsDTO;
import org.umc.travlocksserver.domain.template.exception.code.TemplateSuccessCode;
import org.umc.travlocksserver.domain.template.service.query.TemplateQueryService;
import org.umc.travlocksserver.global.response.SuccessResponse;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController implements TemplateControllerDocs {

    private final TemplateQueryService templateQueryService;

    @GetMapping("/recommendations")
    public ResponseEntity<SuccessResponse<TemplateRecommendationsDTO>> getRecommendedTemplates(
            @AuthenticationPrincipal Long memberId
    ) {
        TemplateRecommendationsDTO response = templateQueryService.getRecommendedTemplates(memberId);
        return ResponseEntity.ok(SuccessResponse.ok(TemplateSuccessCode.TEMPLATE_RECOMMEND_SUCCESS, response));
    }
}
