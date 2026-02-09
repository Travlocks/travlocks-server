package org.umc.travlocksserver.domain.template.dto.response;

import java.util.List;

public record RegionListResponseDTO(
        List<RegionDTO> regions
) {
    public record RegionDTO(
            Long regionId,
            String regionName,
            List<CityDTO> cities
    ) {}

    public record CityDTO(
            Long cityId,
            String cityName
    ) {}

    public static RegionListResponseDTO of(List<RegionDTO> regions) {
        return new RegionListResponseDTO(regions);
    }
}