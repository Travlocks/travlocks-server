package org.umc.travlocksserver.domain.location.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.location.repository.CityRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityQueryService {

    private final CityRepository cityRepository;

    public City getReferenceById(Long cityId) {
        return cityRepository.getReferenceById(cityId);
    }
}
