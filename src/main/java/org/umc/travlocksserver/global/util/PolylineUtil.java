package org.umc.travlocksserver.global.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Polyline 문자열 변환 유틸리티
 * DB의 TEXT 타입 <-> List<List<Double>> 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PolylineUtil {

	private final ObjectMapper objectMapper;

	/**
	 * List<List<Double>>를 JSON 문자열로 변환 (DB 저장용)
	 * @param coordinates 좌표 리스트
	 * @return JSON 문자열
	 */
	public String toString(List<List<Double>> coordinates) {
		try {
			return objectMapper.writeValueAsString(coordinates);
		} catch (Exception e) {
			log.error("Polyline 직렬화 실패: {}", e.getMessage());
			return "[]";
		}
	}

	/**
	 * JSON 문자열을 List<List<Double>>로 변환 (API 응답용)
	 * @param polylineJson JSON 문자열
	 * @return 좌표 리스트
	 */
	public List<List<Double>> toCoordinates(String polylineJson) {
		if (polylineJson == null || polylineJson.trim().isEmpty()) {
			return new ArrayList<>();
		}

		try {
			return objectMapper.readValue(
				polylineJson,
				new TypeReference<List<List<Double>>>() {
				}
			);
		} catch (Exception e) {
			log.error("Polyline 역직렬화 실패: {}", e.getMessage());
			return new ArrayList<>();
		}
	}
}