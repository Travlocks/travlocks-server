package org.umc.travlocksserver.domain.template.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.favorite.repository.FavoriteRepository;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDetailResponseDTO;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateQueryService {

    private final TemplateRepository templateRepository;
    private final FavoriteRepository favoriteRepository;

    /**
     * 인기있는 템플릿 조회
     */
    public List<PopularTemplateResponse> getPopularTemplates(int limit) {

        return templateRepository.findPopularTemplates(PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PopularTemplateResponse toResponse(Template template) {
        return PopularTemplateResponse.builder()
                .templateId(template.getId())
                .coverImageUrl(template.getCoverImageUrl())
                .title(template.getTitle())
                .avgRating(template.getAvgRating())
                .remixCount(template.getRemixCount())
                .travelTheme(template.getTravelTheme().getContent())
                .ownerNickname(template.getOwner().getNickname())
                .build();
    }

    /**
     * 템플릿 상세 조회
     */
    @Transactional(readOnly = true)
    public TemplateDetailResponseDTO getTemplateDetail(Long templateId, Long memberId) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        if (!template.getIsPublic()) {
            throw new TemplateException(TemplateErrorCode.TEMPLATE_NOT_PUBLIC);
        }

        // 즐겨찾기 여부
        boolean isFavorited = false;
        if (memberId != null) {
            isFavorited = favoriteRepository
                    .existsByMemberIdAndTemplateId(memberId, templateId);
        }

        // 태그 목록
        List<String> tags = template.getTemplateTags().stream()
                .map(tt -> tt.getTag().getName())
                .toList();

        // 블록 목록
        List<TemplateDetailResponseDTO.BlockDTO> blocks = template.getTemplateDays().stream()
                .flatMap(day -> day.getTemplateVlocks().stream())
                .map(tv -> new TemplateDetailResponseDTO.BlockDTO(
                        tv.getVlock().getId(),
                        tv.getVlock().getName(),
                        tv.getVlock().getLatitude(),
                        tv.getVlock().getLongitude(),
                        tv.getVlock().getAddress()
                ))
                .toList();

        // 도시명 (첫 번째 도시만)
        String cityName = template.getTemplateCities().stream()
                .map(tc -> tc.getCity().getName())
                .findFirst()
                .orElse("");

        return new TemplateDetailResponseDTO(
                template.getId(),
                template.getTitle(),
                cityName,
                template.getTravelTheme().getContent(),
                template.getOwner().getProfileImageUrl(),
                template.getOwner().getNickname(),
                template.getCoverImageUrl(),
                template.getOwner().getId(),
                template.getAvgRating(),
                template.getTripDays(),
                template.getRemixCount(),
                template.getDescription(),
                tags,
                blocks,
                isFavorited
        );
    }
}
