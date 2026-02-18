package org.umc.travlocksserver.domain.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.entity.TemplateTag;

import java.util.List;

@Repository
public interface TemplateTagRepository extends JpaRepository<TemplateTag, Long> {

    @Query("""
        SELECT tt.tag.name
        FROM TemplateTag tt
        WHERE tt.template.id = :templateId
            AND tt.version = :tagVersion
    """)
    List<String> findByTemplateIdAndVersion(Long templateId, Long tagVersion);
}
