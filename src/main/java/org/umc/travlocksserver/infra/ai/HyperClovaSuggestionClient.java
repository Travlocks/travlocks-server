package org.umc.travlocksserver.infra.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class HyperClovaSuggestionClient implements AiSuggestionClient {

    private final WebClient hyperClovaClient;
    private final ObjectMapper objectMapper;

    @Value("${hyperclova.base-url}")
    private String baseUrl;

    @Value("${hyperclova.model}")
    private String model;

    @Value("${hyperclova.timeout-ms.response}")
    private int timeoutMs;

    @Value("${hyperclova.retry.max}")
    private int retryMax;

    @Value("${hyperclova.retry.backoff-ms}")
    private int retryBackoffMs;

    public Map<Long, Double> requestToAi(Long templateDayId, List<Vlock> usedVlocksInDay, List<Vlock> candidates) {
        String prompt = buildJsonPrompt(templateDayId, usedVlocksInDay, candidates);
        HyperClovaRequestDTO request = HyperClovaRequestDTO.score(prompt);

        Retry retry = Retry.backoff(retryMax, Duration.ofMillis(retryBackoffMs))
                .filter(this::retryable);

        HyperClovaResponseDTO res = hyperClovaClient.post()
                .uri(baseUrl, model)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class)
                                .map(body -> new AiClientException("HyperClova 요청이 거절되었습니다.(4xx)" +
                                        "status=" + r.statusCode() +
                                                " body=" + body))
                )
                .onStatus(HttpStatusCode::is5xxServerError, r ->
                        r.bodyToMono(String.class)
                                .map(body -> new AiClientException("HyperClova 서버에 오류가 발생했습니다.(5xx)" +
                                        "status=" + r.statusCode() +
                                        " body=" + body))
                )
                .bodyToMono(HyperClovaResponseDTO.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .retryWhen(retry)
                .block();

        return parseResponse(res, candidates);
    }

    // 재시도할지를 판별하는 메서드 (타임아웃, 네트워크 문제, 서버오류만 재시도)
    private boolean retryable(Throwable t) {
        if (t instanceof TimeoutException) return true;
        if (t instanceof WebClientRequestException) return true;
        String msg = t.getMessage();
        return msg != null && msg.contains("5xx");
    }

    private String buildJsonPrompt(Long templateDayId, List<Vlock> usedVlocksInDay, List<Vlock> candidates) {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("used", usedVlocksInDay.stream()
                .map(v -> Map.of(
                        "name", v.getName(),
                        "category", v.getVlockCategory().getName(),
                        "stayHours", v.getVlockCategory().getStayHours()
                ))
                .toList()
        );

        payload.put("candidates", candidates.stream()
                .map(v -> Map.of(
                        "id", v.getId(),
                        "name", v.getName(),
                        "category", v.getVlockCategory().getName(),
                        "stayHours", v.getVlockCategory().getStayHours()
                ))
                .toList()
        );

        payload.put("criteria", List.of(
                "일정 흐름을 깨지 않는가",
                "무난하게 끼워 넣기 좋은가",
                "맥락상 어색하지 않은가",
                "무리한 일정은 아닌가?"
        ));

        try {
            return objectMapper.writeValueAsString(payload);  // JSON 문자열로 변경
        } catch (Exception e) {
            throw new IllegalStateException("프롬프트 JSON 직렬화에 실패했습니다.", e);
        }
    }

    // ⚪ HyperClova로부터 온 응답을 가공하는 메서드
    private Map<Long, Double> parseResponse(HyperClovaResponseDTO response, List<Vlock> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Map.of();
        }

        String raw = (response == null) ? null : response.content();

        if (raw == null || raw.isBlank()) {
            return fallback(candidates);
        }

        // 1. 코드 블록 제거
        String s = raw.trim()
                .replaceAll("^```(json)?\\s*", "")
                .replaceAll("\\s*```$", "");

        // 2. JSON만 추출
        int a = s.indexOf('{');  // 첫 {
        int b = s.lastIndexOf('}');  // 마지막 }
        if (a >= 0 && b > a) {
            s = s.substring(a, b + 1);
        } else {
            // JSON 형태가 아니면 fallback
            return fallback(candidates);
        }

        // 3. JSON 파싱
        Map<String, Double> parsed;
        try {
            parsed = objectMapper.readValue(s, new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            return fallback(candidates);
        }

        // 4. 점수 보정 및 결과 반환
        Map<Long, Double> result = new HashMap<>();
        for (Vlock v : candidates) {
            Double score = parsed.get(String.valueOf(v.getId()));
            if (score == null) score = 0.5;  // 누락된 후보 기본 점수 제공
            score = Math.max(0.0, Math.min(1.0, score));  // 점수를 0~1 사이로 보정
            result.put(v.getId(), score);
        }

        return result;
    }

    // ⚪ AI 응답 또는 파싱 등 실패시 기본값으로 보정해주는 메서드
    private Map<Long, Double> fallback(List<Vlock> candidates) {
        Map<Long, Double> fallback = new HashMap<>();
        for (Vlock vlock : candidates) {
            fallback.put(vlock.getId(), 0.5);
        }
        return fallback;
    }
}
