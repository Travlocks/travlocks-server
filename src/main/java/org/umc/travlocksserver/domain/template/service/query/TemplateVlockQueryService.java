package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateVlockQueryService {

	private final TemplateVlockRepository templateVlockRepository;

	public List<TemplateVlock> getAllByTemplateDayIdOrderByOrderNo(Long templateDayId) {
		return templateVlockRepository.findAllByTemplateDayIdOrderByOrderNo(templateDayId);
	}

	public List<Vlock> getDistinctVlocksByTemplateId(Long templateId) {
		return templateVlockRepository.findDistinctVlocksByTemplateId(templateId);
	}
}
