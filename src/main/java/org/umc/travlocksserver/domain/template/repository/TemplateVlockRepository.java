package org.umc.travlocksserver.domain.template.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;

public interface TemplateVlockRepository extends JpaRepository<TemplateVlock, Long> {
	List<TemplateVlock> findByTemplateDayIdIn(Collection<Long> templateDayIds);
}
