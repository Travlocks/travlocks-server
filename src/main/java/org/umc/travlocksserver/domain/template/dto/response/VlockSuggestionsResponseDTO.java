package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.util.List;

public record VlockSuggestionsResponseDTO(
	List<VlockSuggestionCardDTO> vlocks,
	long seed) {

	public record VlockSuggestionCardDTO(
		Long vlockId,
		String name,
		String coverImgUrl,
		String categoryName,
		Double stayhours) {

		public static VlockSuggestionCardDTO from(Vlock vlock, String s3Domain) {
			String coverImgUrl = vlock.getCoverImgUrl();
			if (coverImgUrl == null) {
				coverImgUrl = s3Domain + vlock.getVlockCategory().getDefaultCategoryImageKey();
			}

			return new VlockSuggestionCardDTO(
				vlock.getId(),
				vlock.getName(),
				coverImgUrl,
				vlock.getVlockCategory().getName(),
				vlock.getVlockCategory().getStayHours());
		}
	}

	public static VlockSuggestionsResponseDTO from(List<VlockSuggestionCardDTO> vlocks, long seed) {
		return new VlockSuggestionsResponseDTO(
			vlocks,
			seed);
	}
}
