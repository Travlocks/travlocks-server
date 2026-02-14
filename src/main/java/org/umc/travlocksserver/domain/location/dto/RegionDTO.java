package org.umc.travlocksserver.domain.location.dto;

import org.umc.travlocksserver.domain.location.entity.Region;

public record RegionDTO(
	Long id,
	String name) {
	public static RegionDTO from(Region region) {
		return new RegionDTO(
			region.getId(),
			region.getName());
	}
}
