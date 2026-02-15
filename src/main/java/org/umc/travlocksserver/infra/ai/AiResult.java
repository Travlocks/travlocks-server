package org.umc.travlocksserver.infra.ai;

import java.util.ArrayList;
import java.util.List;

public record AiResult(
		AiTagResponseDTO response,
		long aiLatencyMs
) {
	public record AiTagResponseDTO(
			List<String> cities,
			List<String> free) {
		public AiTagResponseDTO {
			cities = cities == null ? List.of() : cities;
			free = free == null ? List.of() : free;
		}
	}

	public static AiResult of(AiTagResponseDTO response, long aiLatencyMs) {
		return new AiResult(response, aiLatencyMs);
	}
}