package org.umc.travlocksserver.domain.vlock.dto.response;

import org.umc.travlocksserver.domain.vlock.entity.Vlock;

public record VlockBriefDTO(
	Long vlockId,
	String name,
	String category,
	String coverImgUrl
) {
	public static VlockBriefDTO from(Vlock vlock) {
		return new VlockBriefDTO(
			vlock.getId(),
			vlock.getName(),
			vlock.getVlockCategory().getName(),
			vlock.getCoverImgUrl());
	}
}
