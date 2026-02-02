package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.repository.TemplateCityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateCityQueryService {

    private final TemplateCityRepository templateCityRepository;

    public List<Long> getCityIdsByTemplateId(Long templateId) {
        return templateCityRepository.findCityIdsByTemplateId(templateId);
    }
}
