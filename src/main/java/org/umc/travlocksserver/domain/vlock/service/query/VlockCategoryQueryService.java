package org.umc.travlocksserver.domain.vlock.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VlockCategoryQueryService {

    private final VlockCategoryRepository vlockCategoryRepository;

    public Optional<VlockCategory> getByName(String name) {
        return vlockCategoryRepository.findByName(name);
    }
}
