package org.umc.travlocksserver.domain.vlock.dto.response;

import java.util.List;

import org.umc.travlocksserver.infra.redis.vlock.CachedVlockCategoryList;

public record VlockCategoriesDTO(
	List<VlockCategoryDTO> categories) {
	public static VlockCategoriesDTO from(List<VlockCategoryDTO> categories) {
		return new VlockCategoriesDTO(categories);
	}

	public static VlockCategoriesDTO fromCache(CachedVlockCategoryList cache) {
		return new VlockCategoriesDTO(cache.categories());
	}
}
