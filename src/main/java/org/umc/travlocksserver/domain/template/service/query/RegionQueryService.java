package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.location.entity.Region;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.location.repository.RegionRepository;
import org.umc.travlocksserver.domain.template.dto.response.RegionListResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionQueryService {

	private final RegionRepository regionRepository;
	private final CityRepository cityRepository;

	public RegionListResponseDTO getAllRegions() {
		// 1. 모든 Region 조회
		List<Region> regions = regionRepository.findAll();

		// 2. 모든 City 조회 후 Region별로 그룹핑
		List<City> allCities = cityRepository.findAll();
		Map<Long, List<City>> citiesByRegion = allCities.stream()
			.collect(Collectors.groupingBy(city -> city.getRegion().getId()));

		// 3. DTO 변환
		List<RegionListResponseDTO.RegionDTO> regionDTOs = regions.stream()
			.map(region -> {
				List<RegionListResponseDTO.CityDTO> cityDTOs = citiesByRegion.getOrDefault(region.getId(), List.of())
					.stream()
					.map(city -> new RegionListResponseDTO.CityDTO(
						city.getId(),
						city.getName()))
					.toList();

				return new RegionListResponseDTO.RegionDTO(
					region.getId(),
					region.getName(),
					cityDTOs);
			})
			.toList();

		return RegionListResponseDTO.of(regionDTOs);
	}
}
