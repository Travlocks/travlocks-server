package org.umc.travlocksserver.domain.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.template.entity.Template;

public interface TemplateRepository extends JpaRepository<Template, Long> {
}
