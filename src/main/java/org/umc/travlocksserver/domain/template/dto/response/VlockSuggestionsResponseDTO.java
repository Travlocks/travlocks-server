package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

public record VlockSuggestionsResponseDTO(
        List<VlockSuggestionCardDTO> vlocks,
        long seed
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

    public static VlockSuggestionsResponseDTO from(List<VlockSuggestionCardDTO> vlocks, long seed) {
        return new VlockSuggestionsResponseDTO(
                vlocks,
                seed
        );
    }
}
