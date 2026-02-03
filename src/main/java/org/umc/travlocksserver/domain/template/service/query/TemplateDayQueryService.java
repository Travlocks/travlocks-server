package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.exception.TemplateDayException;
import org.umc.travlocksserver.domain.template.exception.code.TemplateDayErrorCode;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateDayQueryService {

    private final TemplateDayRepository templateDayRepository;

    public TemplateDay getTemplateDayAccesible(Long memberId, Long templateId, int dayNo) {
        return templateDayRepository.findTemplateDayAccesible(memberId, templateId, dayNo)
                .orElseThrow(() -> new TemplateDayException(TemplateDayErrorCode.TEMPLATE_DAY_NOT_FOUND));
    }
}
