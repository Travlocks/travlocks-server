package org.umc.travlocksserver.infra.kakao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

// ✨ WebClient을 사용해 KakaoPlaceClient을 실제로 구현한 HTTP 통신 어댑터
@Component
@RequiredArgsConstructor
public class KakaoPlaceWebClient implements KakaoPlaceClient {

    private final WebClient kakaoWebClient;
    @Value("${kakao.keyword-search.path}")
    private String keywordSearchPath;

    @Override
    public List<KakaoPlace> searchPlaces(String query, Double x, Double y, Integer radius, int size) {
        KakaoSearchResponseDTO response = kakaoWebClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(keywordSearchPath)
                            .queryParam("query", query)
                            .queryParam("size", size);

                    if (x != null && y != null) {
                        uriBuilder.queryParam("x", x).queryParam("y", y);
                    }

                    if (radius != null) {
                        uriBuilder.queryParam("radius", radius);
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(KakaoSearchResponseDTO.class)
                .block();

        if (response == null || response.documents() == null) {
            return List.of();
        }

        return response.documents().stream()
                .map(KakaoPlace::from)
                .filter(dto -> dto.latitude() != null && dto.longitude() != null)
                .toList();
    }
}
