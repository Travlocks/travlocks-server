package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

public record VlockSuggestionsResponseDTO(
        Long templateDayId,
        List<VlockSuggestionCardDTO> vlocks,
        long seed,
        boolean fromCached
) {
    public record VlockSuggestionCardDTO(
            Long vlockId,
            String name,
            String coverImgUrl,
            String categoryName,
            Double stayhours
    ) {
        public static VlockSuggestionCardDTO from(Vlock vlock) {
            return new VlockSuggestionCardDTO(
                    vlock.getId(),
                    vlock.getName(),
                    vlock.getCoverImgUrl(),
                    vlock.getVlockCategory().getName(),
                    vlock.getVlockCategory().getStayHours()
            );
        }
    }

    public static VlockSuggestionsResponseDTO from(Long templateDayId, List<VlockSuggestionCardDTO> vlocks, long seed, Boolean fromCached) {
        return new VlockSuggestionsResponseDTO(
                templateDayId,
                vlocks,
                seed,
                fromCached
        );
    }
}
