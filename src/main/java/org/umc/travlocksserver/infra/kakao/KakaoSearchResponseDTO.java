package org.umc.travlocksserver.infra.kakao;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

public record KakaoSearchResponseDTO(
        List<KakaoDocument> documents
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)  // 카카오 JSON은 snakeCase이므로 매핑
    public record KakaoDocument(
            String id,
            String placeName,
            String addressName,
            String roadAddressName,
            String x, String y,
            String categoryGroupName,
            String placeUrl
    ) {}
}
