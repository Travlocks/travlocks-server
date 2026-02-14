package org.umc.travlocksserver.domain.template.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConnectionPortType {
	TOP_LEFT("왼쪽 상단"),
	BOTTOM_LEFT("왼쪽 하단"),
	TOP_RIGHT("오른쪽 상단"),
	BOTTOM_RIGHT("오른쪽 하단");

	private final String description;
}
