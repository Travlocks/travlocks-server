package org.umc.travlocksserver.global.external.odsay;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.umc.travlocksserver.global.external.tmap.TmapDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OdsayApiService {

	private final RestTemplate restTemplate;
	private final OdsayProperties odsayProperties;
	private final ObjectMapper objectMapper;

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
			String rawBody = restTemplate.getForObject(url, String.class);
			if (rawBody == null || rawBody.isBlank()) {
				throw new RuntimeException("ODsay API 응답 바디가 비어있습니다.");
			}

			OdsayResponse response = objectMapper.readValue(rawBody, OdsayResponse.class);

			if (hasError(response.error)) {
				throw new RuntimeException("ODsay API 오류 응답: code=" + response.error.path("code").asText()
					+ ", message=" + response.error.path("message").asText());
			}

			if (response == null
				|| response.result == null
				|| response.result.path == null
				|| response.result.path.isEmpty()) {
				log.warn("ODsay API 응답에 경로 데이터가 없습니다. bodyPreview={}", abbreviateForLog(rawBody, 1000));
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
						List<List<Double>> parsed = parseLinestring(sub.passShape.linestring);
						log.debug("ODsay subPath trafficType={} passShape coords={}", sub.trafficType, parsed.size());
						coordinates.addAll(parsed);
					} else if (sub.startX != null && sub.startY != null
						&& sub.endX != null && sub.endY != null) {
						// passShape 없는 구간(도보 등)은 출발·도착 2점으로 대체
						log.debug("ODsay subPath trafficType={} passShape 없음, 출발/도착 2점 사용", sub.trafficType);
						coordinates.add(List.of(sub.startX, sub.startY));
						coordinates.add(List.of(sub.endX, sub.endY));
					} else {
						log.warn("ODsay subPath trafficType={} 좌표 정보 없음 (passShape=null, startX=null)",
							sub.trafficType);
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
		public JsonNode error;

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
			public Integer trafficType; // 1=지하철, 2=버스, 3=도보
			public Double startX;       // 구간 출발 경도
			public Double startY;       // 구간 출발 위도
			public Double endX;         // 구간 도착 경도
			public Double endY;         // 구간 도착 위도
			public PassShape passShape;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		static class PassShape {
			public String linestring;
		}
	}

	private boolean hasError(JsonNode error) {
		if (error == null || error.isNull()) {
			return false;
		}
		if (error.isArray()) {
			for (JsonNode item : error) {
				if (hasError(item)) {
					return true;
				}
			}
			return false;
		}
		if (error.isObject()) {
			return hasText(error.path("code")) || hasText(error.path("message"));
		}
		return false;
	}

	private boolean hasText(JsonNode node) {
		return node != null && !node.isMissingNode() && !node.isNull() && !node.asText("").isBlank();
	}

	private static String abbreviateForLog(String s, int maxChars) {
		if (s == null || s.length() <= maxChars) {
			return s;
		}
		return s.substring(0, maxChars) + "... (truncated, len=" + s.length() + ")";
	}
}
