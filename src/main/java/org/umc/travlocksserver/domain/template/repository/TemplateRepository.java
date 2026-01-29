package org.umc.travlocksserver.domain.template.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.umc.travlocksserver.domain.template.entity.Template;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    @Query("""
        SELECT t
        FROM Template t
        JOIN FETCH t.travelTheme
        JOIN FETCH t.owner
        WHERE t.isPublic = true
          AND t.deletedAt IS NULL
        ORDER BY
            t.remixCount DESC,
            t.favoriteCount DESC,
            t.avgRating DESC
    """)
    List<Template> findPopularTemplates(Pageable pageable);
}
