package org.umc.travlocksserver.global.geo;

import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

public record LatLng(double lat, double lng) {
	public static LatLng averageFrom(List<Vlock> vlocks) {
		double avgLat = vlocks.stream()
			.mapToDouble(Vlock::getLatitude).average().orElseThrow();
		double avgLng = vlocks.stream()
			.mapToDouble(Vlock::getLongitude).average().orElseThrow();
		return new LatLng(avgLat, avgLng);
	}
}
