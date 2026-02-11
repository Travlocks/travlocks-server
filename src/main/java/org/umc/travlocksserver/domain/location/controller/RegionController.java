package org.umc.travlocksserver.domain.location.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.umc.travlocksserver.domain.template.code.TemplateSuccessCode;
import org.umc.travlocksserver.domain.template.dto.response.RegionListResponseDTO;
import org.umc.travlocksserver.domain.template.service.query.RegionQueryService;
import org.umc.travlocksserver.global.response.SuccessResponse;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionQueryService regionQueryService;  // ← 기존 Service 재사용

    @GetMapping
    public ResponseEntity<SuccessResponse<RegionListResponseDTO>> getRegions() {
        TemplateSuccessCode successCode = TemplateSuccessCode.REGION_RETRIEVE_SUCCESS;
        RegionListResponseDTO response = regionQueryService.getAllRegions();

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, response));
    }
}