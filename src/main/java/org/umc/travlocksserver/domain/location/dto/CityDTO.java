package org.umc.travlocksserver.domain.location.dto;

import org.umc.travlocksserver.domain.location.entity.City;

public record CityDTO(
	Long id,
	String name,
	RegionDTO region
) {
	public static CityDTO from(City city) {
		return new CityDTO(
			city.getId(),
			city.getName(),
			RegionDTO.from(city.getRegion())
		);
	}
}
