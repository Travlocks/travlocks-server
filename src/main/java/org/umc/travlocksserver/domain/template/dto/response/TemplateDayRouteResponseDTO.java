package org.umc.travlocksserver.domain.template.dto.response;

import java.util.List;

import org.umc.travlocksserver.domain.template.enums.TransportType;

public record TemplateDayRouteResponseDTO(
	Long moveTimeId,
	Long fromVlockId,
	Long toVlockId,
	Integer moveMinutes,
	Integer distanceMeter,
	TransportType transportType,
	List<List<Double>> polyline // [[경도, 위도], [경도, 위도], ...]
) {
}