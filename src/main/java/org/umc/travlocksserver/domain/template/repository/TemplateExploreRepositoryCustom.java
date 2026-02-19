package org.umc.travlocksserver.domain.template.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.umc.travlocksserver.domain.template.dto.response.TemplateExploreResponseDTO;
import org.umc.travlocksserver.domain.template.enums.TripDays;

import java.util.List;

public interface TemplateExploreRepositoryCustom {
	Page<TemplateExploreResponseDTO> findExploreTemplatesWithPage(
		String keyword,
		List<String> cityNames,
		List<String> travelThemes,
		List<TripDays> tripDays,
		List<String> transportTypes,
		String sort,
		Pageable pageable);
}
