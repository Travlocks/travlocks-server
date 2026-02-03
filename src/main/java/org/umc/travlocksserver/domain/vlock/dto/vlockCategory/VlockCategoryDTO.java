package org.umc.travlocksserver.domain.vlock.dto.vlockCategory;

import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;

public record VlockCategoryDTO(
	Long id,

	String name,

	Double stayHours
) {
	public static VlockCategoryDTO from(VlockCategory vlockCategory) {
		return new VlockCategoryDTO(
			vlockCategory.getId(),
			vlockCategory.getName(),
			vlockCategory.getStayHours()
		);
	}
}
