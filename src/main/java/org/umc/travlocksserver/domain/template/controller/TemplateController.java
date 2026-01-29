package org.umc.travlocksserver.domain.template.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.template.code.TemplateSuccessCode;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.service.TemplateQueryService;
import org.umc.travlocksserver.global.response.SuccessResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates")
public class TemplateController implements TemplateControllerDocs {

    private final TemplateQueryService templateQueryService;

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
}