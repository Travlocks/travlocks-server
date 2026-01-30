package org.umc.travlocksserver.domain.vlock.repository;

import java.util.List;

import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockResponseDTO;

public interface VlockRepositoryCustom {
	List<VlockResponseDTO> findAllByCityIdOrderByUsageCountDesc(Long cityId);
}
