package org.umc.travlocksserver.domain.template.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.umc.travlocksserver.domain.template.dto.request.TemplatePreInputRequestDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplatePreInputResponseDTO;
import org.umc.travlocksserver.domain.template.enums.TripDays;
import org.umc.travlocksserver.domain.template.service.command.*;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.template.code.TemplateSuccessCode;
import org.umc.travlocksserver.domain.template.dto.request.TemplateRatingCreateRequestDTO;
import org.umc.travlocksserver.domain.template.dto.response.BatchRouteResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCanvasResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDayRouteResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRemixResponseDTO;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.dto.response.OptimizeResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.*;
import org.umc.travlocksserver.domain.template.code.TemplateDaySuccessCode;
import org.umc.travlocksserver.domain.template.dto.response.VlockSuggestionsResponseDTO;
import org.umc.travlocksserver.domain.template.service.query.TemplateCanvasQueryService;
import org.umc.travlocksserver.domain.template.service.query.TemplateQueryService;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.service.query.TemplateRouteQueryService;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDetailResponseDTO;
import org.umc.travlocksserver.global.annotation.LoginUser;
import org.umc.travlocksserver.global.response.SuccessResponse;
import org.umc.travlocksserver.domain.template.dto.request.TemplateVlockAddRequestDTO;
import org.umc.travlocksserver.domain.template.dto.request.TemplateVlockReorderRequestDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateVlockAddResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateVlockDeleteResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateVlockReorderResponseDTO;
import org.umc.travlocksserver.domain.template.service.command.TemplateCommandService;
import org.umc.travlocksserver.domain.template.dto.request.TemplateSaveRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.umc.travlocksserver.domain.template.dto.response.TemplateSaveResponseDTO;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates")
public class TemplateController implements TemplateControllerDocs {

    private final TemplateQueryService templateQueryService;
	private final TemplateDayCommandService templateDayCommandService;
	private final TemplateRemixService templateRemixService;
	private final TemplateCanvasQueryService templateCanvasQueryService;
	private final TemplatePreInputService templatePreInputService;
	private final TemplateDayOptimizeCommandService templateDayOptimizeCommandService;
	private final TemplateRouteQueryService templateRouteQueryService;
	private final TemplateRatingCommandService templateRatingCommandService;
	private final TemplateCommandService templateCommandService;

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

	@PatchMapping(
			value = "/{templateId}",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<SuccessResponse<TemplateSaveResponseDTO>> saveTemplate(
			@PathVariable Long templateId,
			@Valid @RequestPart TemplateSaveRequestDTO request,
			@RequestPart(required = false) MultipartFile coverImage,
			@AuthenticationPrincipal Long memberId
	) {
		TemplateSaveResponseDTO response = templateCommandService.saveTemplate(
				memberId,
				templateId,
				request,
				coverImage
		);

		return ResponseEntity.ok(
				SuccessResponse.ok(TemplateSuccessCode.TEMPLATE_SAVE_SUCCESS, response)
		);
	}

	@PostMapping("/{templateId}/days/{dayNo}/vlocks")
	public ResponseEntity<SuccessResponse<TemplateVlockAddResponseDTO>> addVlock(
			@PathVariable Long templateId,
			@PathVariable Integer dayNo,
			@Valid @RequestBody TemplateVlockAddRequestDTO request,
			@AuthenticationPrincipal Long memberId
	) {
		TemplateVlockAddResponseDTO response = templateDayCommandService.addVlock(
				memberId,
				templateId,
				dayNo,
				request
		);

		return ResponseEntity
				.status(TemplateDaySuccessCode.TEMPLATE_VLOCK_ADD_SUCCESS.getStatus())
				.body(SuccessResponse.ok(TemplateDaySuccessCode.TEMPLATE_VLOCK_ADD_SUCCESS, response));
	}

	@DeleteMapping("/{templateId}/days/{dayNo}/vlocks/{templateVlocksId}")
	public ResponseEntity<SuccessResponse<TemplateVlockDeleteResponseDTO>> deleteVlock(
			@PathVariable Long templateId,
			@PathVariable Integer dayNo,
			@PathVariable Long templateVlocksId,
			@AuthenticationPrincipal Long memberId
	) {
		TemplateVlockDeleteResponseDTO response = templateDayCommandService.deleteVlock(
				memberId,
				templateId,
				dayNo,
				templateVlocksId
		);

		return ResponseEntity.ok(
				SuccessResponse.ok(TemplateDaySuccessCode.TEMPLATE_VLOCK_DELETE_SUCCESS, response)
		);
	}

	@PatchMapping("/{templateId}/days/{dayNo}/vlocks/reorder")
	public ResponseEntity<SuccessResponse<TemplateVlockReorderResponseDTO>> reorderVlocks(
			@PathVariable Long templateId,
			@PathVariable Integer dayNo,
			@Valid @RequestBody TemplateVlockReorderRequestDTO request,
			@AuthenticationPrincipal Long memberId
	) {
		TemplateVlockReorderResponseDTO response = templateDayCommandService.reorderVlocks(
				memberId,
				templateId,
				dayNo,
				request
		);

		return ResponseEntity.ok(
				SuccessResponse.ok(TemplateDaySuccessCode.TEMPLATE_VLOCK_REORDER_SUCCESS, response)
		);
	}

	@PostMapping("/pre-inputs")
	public ResponseEntity<SuccessResponse<TemplatePreInputResponseDTO>> createPreInput(
			@Valid @RequestBody TemplatePreInputRequestDTO request,
			@AuthenticationPrincipal Long memberId
	) {
		TemplateSuccessCode successCode = TemplateSuccessCode.TEMPLATE_PREINPUT_CREATE_SUCCESS;
		TemplatePreInputResponseDTO data = templatePreInputService.createPreInput(memberId, request);

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

	@GetMapping("/{templateId}/vlocks/suggestions")
	public ResponseEntity<SuccessResponse<VlockSuggestionsResponseDTO>> suggestions(
			@AuthenticationPrincipal Long memberId,
			@PathVariable Long templateId
	){
		VlockSuggestionsResponseDTO response = templateDayCommandService.suggestVlocks(memberId, templateId);
		return ResponseEntity.ok(
				SuccessResponse.ok(TemplateDaySuccessCode.VLOCK_SUGGESTION_SUCCESS, response));
	}

	@GetMapping("/{templateId}/days/{dayNo}/optimize")
	public ResponseEntity<SuccessResponse<OptimizeResponseDTO>> optimize(
			@AuthenticationPrincipal Long memberId,
			@PathVariable Long templateId,
			@PathVariable Integer dayNo
	) {
		OptimizeResponseDTO response = templateDayOptimizeCommandService.optimizeVlocks(memberId, templateId, dayNo);
		return ResponseEntity.ok(
				SuccessResponse.ok(TemplateDaySuccessCode.TEMPLATE_DAY_OPTIMIZE_SUCCESS, response));
	}

	@GetMapping("/{templateId}/days/{dayNo}/routes")
	public ResponseEntity<SuccessResponse<BatchRouteResponseDTO>> getRoutes(
			@PathVariable Long templateId,
			@PathVariable Integer dayNo,
			@RequestParam(defaultValue = "WALK") TransportType transportType
	) {
		TemplateSuccessCode successCode = TemplateSuccessCode.TEMPLATE_GET_ROUTES_SUCCESS;

		List<TemplateDayRouteResponseDTO> routes = templateRouteQueryService.getDayRoutes(templateId, dayNo, transportType);

		BatchRouteResponseDTO data = new BatchRouteResponseDTO(routes);

		return ResponseEntity
				.status(successCode.getStatus())
				.body(SuccessResponse.ok(successCode, data));
	}

	@GetMapping("/explore")
	public ResponseEntity<SuccessResponse<List<TemplateExploreResponseDTO>>> exploreTemplates(
			String keyword,
			List<String> cities,
			List<String> themes,
			List<TripDays> tripDays,
			List<String> transportTypes,
			String sort,
			int page
	) {
		return ResponseEntity.ok(
				SuccessResponse.ok(
						TemplateSuccessCode.TEMPLATE_EXPLORE_SUCCESS,
						templateQueryService.exploreTemplates(
								keyword, cities, themes, tripDays, transportTypes, sort, page
						)
				)
		);
	}

	@GetMapping("/recent")
	public ResponseEntity<SuccessResponse<List<TemplateLatestDTO>>> getRecentTemplates(
			@AuthenticationPrincipal Long memberId
	) {
		List<TemplateLatestDTO> templates = templateQueryService.getRecentTemplates(memberId);

		return ResponseEntity.ok(
				SuccessResponse.ok(
						TemplateSuccessCode.TEMPLATE_RECENT_GET_SUCCESS,
						templates
				)
		);
	}

	@PostMapping("/{templateId}/ratings")
	public ResponseEntity<SuccessResponse<Void>> createTemplateRating(
		@PathVariable Long templateId,
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody TemplateRatingCreateRequestDTO request
	) {
		TemplateSuccessCode successCode = TemplateSuccessCode.TEMPLATE_RATING_CREATE_SUCCESS;

		templateRatingCommandService.createTemplateRating(templateId, memberId, request);

		return ResponseEntity
			.status(successCode.getStatus())
			.body(SuccessResponse.ok(successCode));
	}

	@DeleteMapping("/{templateId}")
	public ResponseEntity<SuccessResponse<Void>> deleteTemplate(
			@AuthenticationPrincipal Long memberId,
			@PathVariable Long templateId
	) {
		templateCommandService.deleteById(memberId, templateId);
		return ResponseEntity.ok(
				SuccessResponse.ok(TemplateSuccessCode.TEMPLATE_DELETED_SUCCESS)
		);
	}
}
