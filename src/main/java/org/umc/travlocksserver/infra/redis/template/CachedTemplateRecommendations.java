package org.umc.travlocksserver.infra.redis.template;

import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationCardDTO;

import java.util.List;

public record CachedTemplateRecommendations(
	List<TemplateRecommendationCardDTO> templates) {
	public static CachedTemplateRecommendations from(List<TemplateRecommendationCardDTO> templates) {
		return new CachedTemplateRecommendations(templates);
	}
}
