package org.umc.travlocksserver.global.external.tmap;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmapApiService {

	private final TmapConfig tmapConfig;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	private static final String PEDESTRIAN_PATH = "/tmap/routes/pedestrian";
	private static final int PEDESTRIAN_VERSION = 1;
	private static final String CAR_PATH = "/tmap/routes";

	/**
	 * 도보 경로 조회
	 */
	public TmapDTO.RouteInfo getPedestrianRoute(Double startX, Double startY, Double endX, Double endY) {
		try {
			// 1) 요청 준비
			TmapDTO.PedestrianRequest request = TmapDTO.PedestrianRequest.builder()
				.startX(startX)
				.startY(startY)
				.endX(endX)
				.endY(endY)
				.startName("출발지")
				.endName("도착지")
				.build();

			HttpHeaders headers = new HttpHeaders();
			headers.set("appKey", tmapConfig.getApiKey());
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<TmapDTO.PedestrianRequest> httpEntity = new HttpEntity<>(request, headers);

			// 2) API 호출: String으로 먼저 받고 sanitize 후 파싱
			String url = UriComponentsBuilder
				.fromHttpUrl(tmapConfig.getBaseUrl())
				.path(PEDESTRIAN_PATH)
				.queryParam("version", PEDESTRIAN_VERSION)
				.toUriString();
			log.info("TMAP API 요청: url={}, body={}", url, request);

			ResponseEntity<String> stringResponse = restTemplate.exchange(
				url,
				HttpMethod.POST,
				httpEntity,
				String.class);

			String rawBody = stringResponse.getBody();
			if (rawBody == null || rawBody.isBlank()) {
				throw new RuntimeException("TMAP API 응답 바디가 비어있습니다.");
			}

			boolean hasNullChar = rawBody.indexOf('\u0000') >= 0;
			log.info("TMAP API 응답 수신: status={}, hasNullChar={}, bodyPreview={}",
				stringResponse.getStatusCode(),
				hasNullChar,
				abbreviateForLog(rawBody, 2000));

			String sanitizedBody = sanitizeJson(rawBody);
			if (!sanitizedBody.equals(rawBody)) {
				log.warn("TMAP API 응답에서 제어문자를 제거했습니다. (rawLen={}, sanitizedLen={})",
					rawBody.length(), sanitizedBody.length());
			}

			TmapDTO.PedestrianResponse responseBody = objectMapper.readValue(sanitizedBody,
				TmapDTO.PedestrianResponse.class);

			// 3) 응답 파싱
			if (responseBody == null || responseBody.getFeatures() == null || responseBody.getFeatures().isEmpty()) {
				throw new RuntimeException("TMAP API 응답 features가 비어있습니다.");
			}

			return parseRouteInfo(responseBody);

		} catch (Exception e) {
			log.error("TMAP API 호출 실패: {}", e.getMessage(), e);
			throw new RuntimeException("경로 조회에 실패했습니다: " + e.getMessage(), e);
		}
	}

	/**
	 * 차량 경로 조회
	 */
	public TmapDTO.RouteInfo getCarRoute(Double startX, Double startY, Double endX, Double endY) {
		try {
			TmapDTO.PedestrianRequest request = TmapDTO.PedestrianRequest.builder()
				.startX(startX)
				.startY(startY)
				.endX(endX)
				.endY(endY)
				.startName("출발")
				.endName("도착")
				.build();

			HttpHeaders headers = new HttpHeaders();
			headers.set("appKey", tmapConfig.getApiKey());
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<TmapDTO.PedestrianRequest> httpEntity = new HttpEntity<>(request, headers);

			String url = UriComponentsBuilder
				.fromHttpUrl(tmapConfig.getBaseUrl())
				.path(CAR_PATH)
				.toUriString();
			log.info("TMAP 차량 경로 API 요청: url={}, body={}", url, request);

			ResponseEntity<String> stringResponse = restTemplate.exchange(
				url, HttpMethod.POST, httpEntity, String.class);

			String rawBody = stringResponse.getBody();
			if (rawBody == null || rawBody.isBlank()) {
				throw new RuntimeException("TMAP 차량 경로 API 응답 바디가 비어있습니다.");
			}

			log.info("TMAP 차량 경로 API 응답: status={}, bodyPreview={}",
				stringResponse.getStatusCode(),
				abbreviateForLog(rawBody, 2000));

			String sanitizedBody = sanitizeJson(rawBody);
			TmapDTO.PedestrianResponse responseBody = objectMapper.readValue(sanitizedBody,
				TmapDTO.PedestrianResponse.class);

			if (responseBody == null || responseBody.getFeatures() == null || responseBody.getFeatures().isEmpty()) {
				throw new RuntimeException("TMAP 차량 경로 API 응답 features가 비어있습니다.");
			}

			return parseCarRouteInfo(responseBody);

		} catch (Exception e) {
			log.error("TMAP 차량 경로 API 호출 실패: {}", e.getMessage(), e);
			throw new RuntimeException("차량 경로 조회에 실패했습니다: " + e.getMessage(), e);
		}
	}

	/**
	 * TMAP 응답을 RouteInfo로 변환
	 */
	private TmapDTO.RouteInfo parseRouteInfo(TmapDTO.PedestrianResponse response) {
		Integer totalDistance = null;
		Integer totalTime = null;
		List<List<Double>> coordinates = new ArrayList<>();

		for (TmapDTO.PedestrianResponse.Feature feature : response.getFeatures()) {
			// 첫 번째 Feature에서 총 거리/시간 추출
			if (feature.getProperties() != null) {
				if (totalDistance == null && feature.getProperties().getTotalDistance() != null) {
					totalDistance = feature.getProperties().getTotalDistance();
				}
				if (totalTime == null && feature.getProperties().getTotalTime() != null) {
					totalTime = feature.getProperties().getTotalTime();
				}
			}

			// LineString 타입의 좌표 수집
			if (feature.getGeometry() != null && "LineString".equals(feature.getGeometry().getType())) {
				@SuppressWarnings("unchecked") List<List<Double>> lineCoords = (List<List<Double>>)feature.getGeometry()
					.getCoordinates();
				if (lineCoords != null) {
					coordinates.addAll(lineCoords);
				}
			}
		}

		if (totalDistance == null || totalTime == null) {
			throw new RuntimeException("경로 정보를 파싱할 수 없습니다.");
		}

		// Polyline 인코딩 (좌표 리스트를 문자열로 변환)
		String polyline = encodePolyline(coordinates);

		return TmapDTO.RouteInfo.builder()
			.totalDistanceMeter(totalDistance)
			.totalTimeMinutes((int)Math.ceil(totalTime / 60.0)) // 초 -> 분 (올림)
			.polyline(polyline)
			.build();
	}

	/**
	 * 차량 경로 응답을 RouteInfo로 변환
	 * EP(도착) 포인트에서 총 거리/시간 추출
	 */
	private TmapDTO.RouteInfo parseCarRouteInfo(TmapDTO.PedestrianResponse response) {
		Integer totalDistance = null;
		Integer totalTime = null;
		List<List<Double>> coordinates = new ArrayList<>();

		for (TmapDTO.PedestrianResponse.Feature feature : response.getFeatures()) {
			if (feature.getGeometry() != null && "LineString".equals(feature.getGeometry().getType())) {
				@SuppressWarnings("unchecked") List<List<Double>> lineCoords = (List<List<Double>>)feature.getGeometry()
					.getCoordinates();
				if (lineCoords != null) {
					coordinates.addAll(lineCoords);
				}
			}

			if (feature.getProperties() != null
				&& feature.getProperties().getTotalTime() != null
				&& totalTime == null) {
				totalDistance = feature.getProperties().getTotalDistance();
				totalTime = feature.getProperties().getTotalTime();
			}
		}

		if (totalDistance == null || totalTime == null) {
			throw new RuntimeException("차량 경로 정보를 파싱할 수 없습니다.");
		}

		return TmapDTO.RouteInfo.builder()
			.totalDistanceMeter(totalDistance)
			.totalTimeMinutes((int)Math.ceil(totalTime / 60.0))
			.polyline(encodePolyline(coordinates))
			.build();
	}

	/**
	 * 좌표 리스트를 polyline 문자열로 인코딩
	 * 간단한 JSON 배열 형태로 저장
	 */
	private String encodePolyline(List<List<Double>> coordinates) {
		if (coordinates == null || coordinates.isEmpty()) {
			return "[]";
		}

		// 간단한 JSON 형태로 저장
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

	/**
	 * JSON 문자열에 포함되면 Jackson 파싱을 깨뜨리는 제어문자(0x00~0x1F) 제거
	 */
	private static String sanitizeJson(String raw) {
		if (raw == null)
			return null;
		// \x00-\x1F 제어문자 중 \t(0x09), \n(0x0A), \r(0x0D) 제외
		return raw.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
	}

	private static String abbreviateForLog(String s, int maxChars) {
		if (s == null)
			return null;
		if (s.length() <= maxChars)
			return s;
		return s.substring(0, maxChars) + "... (truncated, len=" + s.length() + ")";
	}
}
