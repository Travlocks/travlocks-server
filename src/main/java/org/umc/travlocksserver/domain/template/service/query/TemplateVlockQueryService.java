package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateVlockQueryService {

    private final TemplateVlockRepository templateVlockRepository;

    public List<Vlock> getDistinctVlocksByTemplateDayId(Long templateDayId) {
        return templateVlockRepository.findDistinctVlocksByTemplateDayId(templateDayId);
    }

    public List<Long> getAllVlockIdsByTemplateDayTemplateId(Long templateId) {
        return templateVlockRepository.findAllVlockIdsByTemplateDayTemplateId(templateId);
    }
}
