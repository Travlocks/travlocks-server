package org.umc.travlocksserver.domain.vlock.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VlockQueryService {

    private final VlockRepository vlockRepository;

    public List<Vlock> getPopularByCityIds(List<Long> cityIds, Pageable pageable) {
        return vlockRepository.findPopularByCityIds(cityIds, pageable);
    }

    public List<Vlock> getAllById(List<Long> ids) {
        return vlockRepository.findAllById(ids);
    }

    public List<Vlock> getVlocksInBoxExcluding(
            List<Long> cityIds,
            List<Long> excludeVlockIds,
            double minLat,
            double maxLat,
            double minLng,
            double maxLng,
            Pageable pageable
    ) {
        return vlockRepository.findVlocksInBoxExcluding(cityIds, excludeVlockIds, minLat, maxLat, minLng, maxLng, pageable);
    }
}
