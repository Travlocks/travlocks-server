package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.entity.TemplateTag;
import org.umc.travlocksserver.domain.template.repository.TemplateTagRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateTagQueryService {

    private final TemplateTagRepository templateTagRepository;

    public List<String> getTemplateTags(Long templateId, Long tagVersion) {
        return templateTagRepository.findByTemplateIdAndVersion(templateId, tagVersion);
    }
}
