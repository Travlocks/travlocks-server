package org.umc.travlocksserver.domain.vlock.dto.response;

public record VlockBriefDTO(
	Long vlockId,
	String name,
	String category
) {
}