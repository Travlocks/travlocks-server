package org.umc.travlocksserver.domain.favorite.dto;

public record FavoriteResponseDto(
        Long templateId,
        boolean isFavorited,
        int favoriteCount
) {
}