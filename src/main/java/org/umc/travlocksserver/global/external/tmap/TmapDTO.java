package org.umc.travlocksserver.global.external.tmap;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TmapDTO {

	/**
	 * TMAP API 요청 DTO
	 */
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class PedestrianRequest {
		@JsonProperty("startX")
		private Double startX; // 출발지 경도

		@JsonProperty("startY")
		private Double startY; // 출발지 위도

		@JsonProperty("endX")
		private Double endX; // 도착지 경도

		@JsonProperty("endY")
		private Double endY; // 도착지 위도

		@JsonProperty("startName")
		private String startName; // 출발지 이름

		@JsonProperty("endName")
		private String endName; // 도착지 이름
	}

	/**
	 * TMAP API 응답 DTO
	 */
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PedestrianResponse {
		private String type;
		private List<Feature> features;

		@Getter
		@NoArgsConstructor
		@AllArgsConstructor
		public static class Feature {
			private String type;
			private Geometry geometry;
			private Properties properties;
		}

		@Getter
		@NoArgsConstructor
		@AllArgsConstructor
		public static class Geometry {
			private String type;
			private Object coordinates; // LineString일 때 List<List<Double>>, Point일 때 List<Double>
		}

		@Getter
		@NoArgsConstructor
		@AllArgsConstructor
		@JsonIgnoreProperties(ignoreUnknown = true)  // 알 수 없는 필드 무시
		public static class Properties {
			private Integer totalDistance; // 전체 거리(m)
			private Integer totalTime; // 전체 시간(초)
			private Integer index; // 경로 순번
			private Integer pointIndex; // 포인트 순번
			private String name; // 도로명
			private String description; // 설명
			private String direction; // 방향
			private Integer distance; // 구간 거리
			private Integer time; // 구간 시간
			private String lineIndex; // 라인 인덱스
		}
	}

	/**
	 * 경로 정보 DTO (서비스 레이어에서 사용)
	 */
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class RouteInfo {
		private Integer totalDistanceMeter; // 총 거리 (미터)
		private Integer totalTimeMinutes; // 총 시간 (분)
		private String polyline; // 인코딩된 polyline 문자열
	}
}