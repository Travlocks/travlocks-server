package org.umc.travlocksserver.domain.template.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.template.exception.code.TemplateDaySuccessCode;
import org.umc.travlocksserver.domain.template.dto.response.VlockSuggestionsResponseDTO;
import org.umc.travlocksserver.domain.template.service.command.TemplateDayCommandService;
import org.umc.travlocksserver.global.response.SuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/template-days")
public class TemplateDayController implements TemplateDayControllerDocs {

    private final TemplateDayCommandService templateDayCommandService;

    @GetMapping("/{templateDayId}/vlocks/suggestions")
    public ResponseEntity<SuccessResponse<VlockSuggestionsResponseDTO>> suggestions(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long templateDayId
    ){
        VlockSuggestionsResponseDTO response = templateDayCommandService.suggestVlocks(memberId, templateDayId);
        return ResponseEntity.ok(
                SuccessResponse.ok(TemplateDaySuccessCode.VLOCK_SUGGESTION_SUCCESS, response));
    }
}
