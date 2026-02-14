package org.umc.travlocksserver.domain.vlock.dto.command;

import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;

public record VlockUpdateCommand(
	VlockCategory category,
	City city,
	String name,
	Double latitude,
	Double longitude,
	String address,
	String memo,
	String coverImgUrl,
	Boolean isPublic) {
}
