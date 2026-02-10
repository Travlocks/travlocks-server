package org.umc.travlocksserver.domain.vlock.dto.command;

import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;

public record VlockCreateCommand(
	VlockCategory category,
	City city,
	Member owner,
	String name,
	String address,
	String memo,
	String coverImgUrl,
	Double latitude,
	Double longitude
) {
}
