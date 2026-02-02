package org.umc.travlocksserver.domain.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

@Repository
public interface TemplateVlockRepository extends JpaRepository<TemplateVlock, Long> {

    @Query("""
        SELECT DISTINCT v
        FROM TemplateVlock tv
            JOIN tv.vlock v
            JOIN FETCH v.vlockCategory
        WHERE tv.templateDay.id = :templateDayId
    """)
    List<Vlock> findDistinctVlocksByTemplateDayId(@Param("templateDayId") Long templateDayId);

    List<Long> findAllVlockIdsByTemplateDayTemplateId(Long templateId);
}
