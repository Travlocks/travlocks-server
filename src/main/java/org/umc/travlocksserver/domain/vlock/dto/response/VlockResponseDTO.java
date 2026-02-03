package org.umc.travlocksserver.domain.vlock.dto.response;

import java.time.LocalDateTime;

import org.umc.travlocksserver.domain.location.dto.CityDTO;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

public record VlockResponseDTO(
	Long id,
	Long memberId,

	VlockCategoryDTO vlockCategory,

	CityDTO city,

	String name,
	String address,
	String memo,
	String coverImgUrl,
	String linkUrl,

	Double latitude,
	Double longitude,

	Integer usageCount,

	Boolean isPublic,

	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static VlockResponseDTO from(Vlock vlock) {
		return new VlockResponseDTO(
			vlock.getId(),
			vlock.getOwner().getId(),

			VlockCategoryDTO.from(vlock.getVlockCategory()),

			CityDTO.from(vlock.getCity()),

			vlock.getName(),
			vlock.getAddress(),
			vlock.getMemo(),
			vlock.getCoverImgUrl(),
			vlock.getLinkUrl(),

			vlock.getLatitude(),
			vlock.getLongitude(),

			vlock.getUsageCount(),

			vlock.getIsPublic(),

			vlock.getCreatedAt(),
			vlock.getUpdatedAt()
		);
	}
}
