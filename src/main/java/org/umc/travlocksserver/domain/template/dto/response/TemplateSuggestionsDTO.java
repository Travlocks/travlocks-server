package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.infra.redis.template.CachedTemplateSuggestions;

import java.util.List;

public record TemplateSuggestionsDTO(
	List<TemplateSuggestionCardDTO> templates) {
	public static TemplateSuggestionsDTO from(CachedTemplateSuggestions cached) {
		return new TemplateSuggestionsDTO(cached.templates());
	}
}
