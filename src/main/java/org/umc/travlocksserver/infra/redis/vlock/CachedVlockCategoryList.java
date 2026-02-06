package org.umc.travlocksserver.infra.redis.vlock;

import java.util.List;

import org.umc.travlocksserver.domain.vlock.dto.response.VlockCategoryDTO;

public record CachedVlockCategoryList(
	List<VlockCategoryDTO> categories
) {
	public static CachedVlockCategoryList from(List<VlockCategoryDTO> categories) {
		return new CachedVlockCategoryList(categories);
	}
}
