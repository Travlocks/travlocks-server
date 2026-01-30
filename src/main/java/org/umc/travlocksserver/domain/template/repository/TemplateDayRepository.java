package org.umc.travlocksserver.domain.template.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;

public interface TemplateDayRepository extends JpaRepository<TemplateDay, Long> {
	List<TemplateDay> findByTemplateIdOrderByDayNoAsc(Long templateId);

	Optional<TemplateDay> findByTemplateIdAndDayNo(Long templateId, Integer dayNo);
}
