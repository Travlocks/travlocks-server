package org.umc.travlocksserver.global.external.odsay;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.umc.travlocksserver.global.external.tmap.TmapDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OdsayApiService {

	private final RestTemplate restTemplate;
	private final OdsayProperties odsayProperties;

	private static final String BASE_URL = "https://api.odsay.com/v1/api";
	private static final String TRANSIT_PATH = "/searchPubTransPathT";

	public TmapDTO.RouteInfo getTransitRoute(
		Double startX, Double startY,
		Double endX, Double endY) {

		String url = UriComponentsBuilder
			.fromHttpUrl(BASE_URL + TRANSIT_PATH)
			.queryParam("apiKey", odsayProperties.apiKey())
			.queryParam("SX", startX)
			.queryParam("SY", startY)
			.queryParam("EX", endX)
			.queryParam("EY", endY)
			.queryParam("SearchType", 0)
			.queryParam("SearchPathType", 0)
			.toUriString();

		log.info("ODsay 대중교통 경로 API 요청: startX={}, startY={}, endX={}, endY={}", startX, startY, endX, endY);

		try {
			OdsayResponse response = restTemplate.getForObject(url, OdsayResponse.class);

			if (response == null
				|| response.result == null
				|| response.result.path == null
				|| response.result.path.isEmpty()) {
				throw new RuntimeException("ODsay API 응답에 경로 데이터가 없습니다.");
			}

			OdsayResponse.Path best = response.result.path.get(0);

			if (best.info == null) {
				throw new RuntimeException("ODsay API 경로 정보를 파싱할 수 없습니다.");
			}

			List<List<Double>> coordinates = new ArrayList<>();
			if (best.subPath != null) {
				for (OdsayResponse.SubPath sub : best.subPath) {
					if (sub.passShape != null && sub.passShape.linestring != null) {
						coordinates.addAll(parseLinestring(sub.passShape.linestring));
					}
				}
			}

			log.info("ODsay 대중교통 경로 API 응답: totalTime={}분, totalDistance={}m, coords={}",
				best.info.totalTime, best.info.totalDistance, coordinates.size());

			return TmapDTO.RouteInfo.builder()
				.totalTimeMinutes(best.info.totalTime)
				.totalDistanceMeter(best.info.totalDistance)
				.polyline(encodePolyline(coordinates))
				.build();

		} catch (Exception e) {
			log.error("ODsay 대중교통 경로 API 호출 실패: {}", e.getMessage(), e);
			throw new RuntimeException("대중교통 경로 조회에 실패했습니다: " + e.getMessage(), e);
		}
	}

	private List<List<Double>> parseLinestring(String linestring) {
		List<List<Double>> coords = new ArrayList<>();
		String[] points = linestring.trim().split(" ");
		for (String point : points) {
			String[] lonLat = point.split(",");
			if (lonLat.length == 2) {
				try {
					double lon = Double.parseDouble(lonLat[0].trim());
					double lat = Double.parseDouble(lonLat[1].trim());
					coords.add(List.of(lon, lat));
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return coords;
	}

	private String encodePolyline(List<List<Double>> coordinates) {
		if (coordinates.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < coordinates.size(); i++) {
			List<Double> coord = coordinates.get(i);
			sb.append(String.format("[%.6f,%.6f]", coord.get(0), coord.get(1)));
			if (i < coordinates.size() - 1) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	// ── 응답 DTO ────────────────────────────────────────────────────────────

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class OdsayResponse {
		public Result result;

		@JsonIgnoreProperties(ignoreUnknown = true)
		static class Result {
			public List<Path> path;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		static class Path {
			public Info info;
			public List<SubPath> subPath;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		static class Info {
			public Integer totalTime;     // 분 단위
			public Integer totalDistance; // 미터 단위
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		static class SubPath {
			public PassShape passShape;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		static class PassShape {
			public String linestring;
		}
	}
}
