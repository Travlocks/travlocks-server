package org.umc.travlocksserver.domain.template.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.entity.Template;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long>, TemplateRepositoryCustom {

    @Query("""
        SELECT DISTINCT t.parentTemplate.id
        FROM Template t
        WHERE t.owner.id = :memberId
            AND t.parentTemplate IS NOT NULL
    """)
    List<Long> findRemixedTemplateIdsByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT t.travelTheme.id
        FROM Template t
        WHERE t.owner.id = :memberId
        ORDER BY t.updatedAt DESC
    """)
    List<Long> findRecentThemeIdsByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
