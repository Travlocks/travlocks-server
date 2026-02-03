package org.umc.travlocksserver.domain.vlock.repository;

import java.util.List;

import org.umc.travlocksserver.domain.vlock.dto.response.VlockResponseDTO;

public interface VlockRepositoryCustom {

	List<VlockResponseDTO> findPopularVlocks(Long cityId);

	List<VlockResponseDTO> findCategoryVlocks(Long cityId,Long categoryId);
}
