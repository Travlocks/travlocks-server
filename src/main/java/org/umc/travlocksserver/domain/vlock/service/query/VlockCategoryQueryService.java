package org.umc.travlocksserver.domain.vlock.service.query;

import static org.umc.travlocksserver.domain.vlock.code.VlockCategoryErrorCode.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockCategoriesDTO;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockCategoryDTO;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.exception.VlockCategoryException;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;
import org.umc.travlocksserver.infra.redis.vlock.CachedVlockCategoryList;
import org.umc.travlocksserver.infra.redis.vlock.VlockCategoryCache;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VlockCategoryQueryService {

	private final VlockCategoryRepository vlockCategoryRepository;
	private final VlockCategoryCache vlockCategoryCache;

	public Optional<VlockCategory> getByName(String name) {
		return vlockCategoryRepository.findByName(name);
	}

	public VlockCategoriesDTO getAllCategories() {
		CachedVlockCategoryList cachedList = vlockCategoryCache.getAll();

		if (cachedList != null && !cachedList.categories().isEmpty()) {
			return VlockCategoriesDTO.fromCache(cachedList);
		}

		List<VlockCategoryDTO> categories = vlockCategoryRepository.findAll().stream()
			.map(VlockCategoryDTO::from)
			.toList();

		if (categories.isEmpty()) {
			throw new VlockCategoryException(DEFAULT_VLOCK_CATEGORY_NOT_FOUND);
		}

		vlockCategoryCache.setAll(CachedVlockCategoryList.from(categories));

		return VlockCategoriesDTO.from(categories);
	}
}
