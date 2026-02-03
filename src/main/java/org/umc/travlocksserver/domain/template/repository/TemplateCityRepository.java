package org.umc.travlocksserver.domain.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.entity.TemplateCity;
import org.umc.travlocksserver.domain.template.projection.CityProjectionDTO;

@Repository
public interface TemplateCityRepository extends JpaRepository<TemplateCity, Long> {
	List<TemplateCity> findByTemplateId(Long templateId);

    @Query("""
        SELECT tc.city.id
        FROM TemplateCity tc
        WHERE tc.template.id = :templateId
    """)
    List<Long> findCityIdsByTemplateId(@Param("templateId") Long templateId);

    @Query("""
        SELECT new org.umc.travlocksserver.domain.template.projection.CityProjectionDTO(c.id, c.name)
        FROM TemplateCity tc
            JOIN tc.city c
        WHERE tc.template.id = :templateId
    """)
    List<CityProjectionDTO> findCitiesByTemplateId(@Param("templateId") Long templateId);
}
