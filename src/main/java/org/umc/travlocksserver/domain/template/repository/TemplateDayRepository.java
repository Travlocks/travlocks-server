package org.umc.travlocksserver.domain.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;

import java.util.Optional;

@Repository
public interface TemplateDayRepository extends JpaRepository<TemplateDay, Long> {

    Optional<TemplateDay> findByIdAndTemplateOwnerId(Long templateDayId, Long memberId);
}
