package org.umc.travlocksserver.domain.template.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;

@Repository
public interface TemplateDayRepository extends JpaRepository<TemplateDay, Long> {

    List<TemplateDay> findByTemplateIdOrderByDayNoAsc(Long templateId);

    @Query("""
        SELECT td
        FROM TemplateDay td
            JOIN td.template t
            JOIN t.owner m
        WHERE t.id = :templateId
            AND td.dayNo = :dayNo
            AND m.id = :memberId
    """)
    Optional<TemplateDay> findTemplateDayAccesible(Long memberId, Long templateDayId, Integer dayNo);
	Optional<TemplateDay> findByTemplateIdAndDayNo(Long templateId, Integer dayNo);
}
