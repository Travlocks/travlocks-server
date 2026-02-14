package org.umc.travlocksserver.domain.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.entity.TemplateTag;

@Repository
public interface TemplateTagRepository extends JpaRepository<TemplateTag, Long> {}
