package org.umc.travlocksserver.infra.ai.dto;

import java.util.List;

public record AiTagResponseDTO(
        List<String> cities,
        List<String> free) {

    public AiTagResponseDTO {
        cities = cities == null ? List.of() : cities;
        free = free == null ? List.of() : free;
    }
}
