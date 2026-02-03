package org.umc.travlocksserver.domain.template.dto.response;

import java.util.List;

public record BatchRouteResponseDTO(
	List<TemplateDayRouteResponseDTO> routes
) {
}