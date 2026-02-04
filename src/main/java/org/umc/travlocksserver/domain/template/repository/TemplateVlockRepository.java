package org.umc.travlocksserver.domain.template.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

@Repository
public interface TemplateVlockRepository extends JpaRepository<TemplateVlock, Long> {
	List<TemplateVlock> findByTemplateDayIdIn(Collection<Long> templateDayIds);

	List<TemplateVlock> findByTemplateDayIdOrderByOrderNo(Long templateDayId);

    @Query("""
        SELECT DISTINCT v
        FROM TemplateVlock tv
            JOIN tv.vlock v
            JOIN FETCH v.vlockCategory
        WHERE tv.templateDay.id = :templateDayId
    """)
    List<Vlock> findDistinctVlocksByTemplateDayId(@Param("templateDayId") Long templateDayId);

    List<Long> findAllVlockIdsByTemplateDayTemplateId(Long templateId);

	@Query("""
			select tv
			from TemplateVlock tv
			join fetch tv.vlock v
			where tv.templateDay.id = :templateDayId
			order by tv.orderNo asc
		""")
	List<TemplateVlock> findAllByTemplateDayIdFetchVlock(Long templateDayId);

	@Query("""
		SELECT tv.vlock
		FROM TemplateVlock tv
		WHERE tv.templateDay.template.id = :templateId
	""")
    List<Vlock> findVlocksByTemplateId(Long templateId);
}
