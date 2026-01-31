package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateQueryService {

    private final TemplateRepository templateRepository;

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
}
