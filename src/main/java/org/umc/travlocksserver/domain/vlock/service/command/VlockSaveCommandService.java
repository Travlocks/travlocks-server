package org.umc.travlocksserver.domain.vlock.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.infra.kakao.KakaoPlace;

@Service
@RequiredArgsConstructor
public class VlockSaveCommandService {

    private final VlockRepository vlockRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveVlocksFromExternal(KakaoPlace place, VlockCategory category, City city) {
        try {
            Vlock vlock = Vlock.createByExternal(
                    place.placeId(),
                    category,
                    city,
                    place.name(),
                    place.latitude(),
                    place.longitude(),
                    place.address()
            );

            vlockRepository.save(vlock);
        } catch(
                DataIntegrityViolationException e) {
            // UNIQUE 충돌 -> 이미 존재하는 블록 -> 무시
        }
    }
}
