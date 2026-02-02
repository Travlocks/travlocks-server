package org.umc.travlocksserver.domain.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.travlocksserver.domain.template.entity.TemplateCity;

public interface TemplateCityRepository extends JpaRepository<TemplateCity, Long> {
	List<TemplateCity> findByTemplateId(Long templateId);
}
