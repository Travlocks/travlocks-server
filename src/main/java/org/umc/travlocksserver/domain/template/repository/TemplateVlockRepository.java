package org.umc.travlocksserver.domain.template.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;

public interface TemplateVlockRepository extends JpaRepository<TemplateVlock, Long> {
	List<TemplateVlock> findByTemplateDayIdIn(Collection<Long> templateDayIds);

	@Query("""
			select tv
			from TemplateVlock tv
			join fetch tv.vlock v
			where tv.templateDay.id = :templateDayId
			order by tv.orderNo asc
		""")
	List<TemplateVlock> findAllByTemplateDayIdFetchVlock(Long templateDayId);
}
