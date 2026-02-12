package org.umc.travlocksserver.global.geo;

// ✨ 공간 범위(위도/경도)를 표현하는 Value Object
public record BoundingBox(
	double minLat,
	double maxLat,
	double minLng,
	double maxLng) {
}
