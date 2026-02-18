package org.umc.travlocksserver.infra.redis.template;

import org.umc.travlocksserver.domain.template.dto.response.TemplateSuggestionCardDTO;

import java.util.List;

public record CachedTemplateSuggestions(
	List<TemplateSuggestionCardDTO> templates) {
	public static CachedTemplateSuggestions from(List<TemplateSuggestionCardDTO> templates) {
		return new CachedTemplateSuggestions(templates);
	}
}
