package org.umc.travlocksserver.domain.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.umc.travlocksserver.domain.template.dto.response.RegionListResponseDTO;
import org.umc.travlocksserver.global.response.SuccessResponse;

@Tag(name = "Region API", description = "지역 API")
public interface RegionControllerDocs {

    @Operation(
            summary = "여행지 조회 API",
            description = """
			여행지를 조회합니다.
			"""
    )
    @ApiResponse(responseCode = "200", description = "여행지 목록 조회에 성공했습니다.")
    public ResponseEntity<SuccessResponse<RegionListResponseDTO>> getRegions();
}
