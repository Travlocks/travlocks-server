package org.umc.travlocksserver.domain.template.repository;

import org.umc.travlocksserver.domain.template.dto.response.TemplateSuggestionCardDTO;

import java.util.List;

public interface TemplateRepositoryCustom {

	List<TemplateSuggestionCardDTO> suggestPersonalized(
		List<Long> preferredThemeIds,
		List<Long> recentThemeIds,
		List<Long> excludedTemplateIds,
		int limit);
}
