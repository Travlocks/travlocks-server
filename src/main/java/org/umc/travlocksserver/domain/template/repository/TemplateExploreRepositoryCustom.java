package org.umc.travlocksserver.domain.template.repository;

import org.umc.travlocksserver.domain.template.dto.response.TemplateExploreResponseDTO;
import org.umc.travlocksserver.domain.template.enums.TripDays;

import java.util.List;

public interface TemplateExploreRepositoryCustom {
	List<TemplateExploreResponseDTO> findExploreTemplates(
		String keyword,
		List<String> cityNames,
		List<String> travelThemes,
		List<TripDays> tripDays,
		List<String> transportTypes,
		String sort,
		int offset);
}
