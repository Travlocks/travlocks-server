package org.umc.travlocksserver.infra.ai;

import java.util.List;

public record AiTagResponseDTO(
	List<String> cities,
	List<String> free) {
}
