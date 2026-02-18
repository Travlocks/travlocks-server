package org.umc.travlocksserver.infra.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.infra.ai.util.AiPromptProvider;
import org.umc.travlocksserver.infra.ai.dto.AiRequestDTO;
import org.umc.travlocksserver.infra.ai.dto.AiResponseDTO;
import org.umc.travlocksserver.infra.ai.dto.AiTagResponseDTO;
import org.umc.travlocksserver.infra.ai.exception.AiErrorCode;
import org.umc.travlocksserver.infra.ai.exception.AiException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!benchmark")
public class HyperClovaSuggestionClient implements AiSuggestionClient {

	private final WebClient hyperClovaClient;
	private final ObjectMapper objectMapper;
	private final AiPromptProvider aiPromptProvider;

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

	/**
	 * 외부 AI API를 통해 추천된 Vlock들을 받아오는 메서드
	 */
	public Map<Long, Double> suggestVlocks(List<Vlock> usedVlocksInTemplate, List<Vlock> candidates) {
		String systemPrompt = aiPromptProvider.getSystemPromptForVlockSuggestion();
		String userPrompt = aiPromptProvider.buildUserPromptForVlockSuggestion(usedVlocksInTemplate, candidates);
		AiRequestDTO request = AiRequestDTO.of(systemPrompt, userPrompt);

		AiResponseDTO response = requestToAi(request);

		return parseVlockSuggestionResponse(response, candidates);
	}

	/**
	 * 외부 AI API를 통해 생성된 태그들을 받아오는 메서드
	 */
	@Override
	public AiTagResponseDTO generateTags(
			String region,
			List<String> fixedTags,
			List<String> cityCandidates,
			List<Vlock> vlocksInTemplate
	) {
		String systemPrompt = aiPromptProvider.getSystemPromptForTagGeneration();
		String userPrompt = aiPromptProvider.buildUserPromptForTagGeneration(region, fixedTags, cityCandidates, vlocksInTemplate);
		AiRequestDTO request = AiRequestDTO.of(systemPrompt, userPrompt);

		AiResponseDTO response = requestToAi(request);

		return parseTagResponse(response);
	}

	/**
	 * 외부 AI API에 요청을 보내는 메서드
	 */
	private AiResponseDTO requestToAi(AiRequestDTO request) {
		Retry retry = Retry.backoff(retryMax, Duration.ofMillis(retryBackoffMs))
				.filter(this::isRetryable);

		return hyperClovaClient.post()
				.uri(baseUrl, model)
				.bodyValue(request)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, r ->
						r.bodyToMono(String.class).flatMap(body -> {
							log.error("AI CLIENT 요청 형식 오류: {}", body);
							return Mono.error(new AiException(AiErrorCode.AI_INVALID_REQUEST));
						})
				)
				.onStatus(HttpStatusCode::is5xxServerError, r ->
						r.bodyToMono(String.class).flatMap(body -> {
							log.error("AI CLIENT 서버 내부 오류: {}", body);
							return Mono.error(new AiException(AiErrorCode.AI_SERVER_ERROR));
						})
				)
				.bodyToMono(AiResponseDTO.class)
				.timeout(Duration.ofMillis(timeoutMs))
				.onErrorMap(TimeoutException.class, e -> new AiException(AiErrorCode.AI_TIMEOUT))
				.onErrorMap(t -> !(t instanceof AiException), t -> new AiException(AiErrorCode.AI_SERVER_ERROR))
				.retryWhen(retry)
				.block();
	}

	/**
	 * 에러에 대해 재시도 여부를 판별하는 메서드
	 * - 일시적인 네트워크 장애, 5xx 에러 타임아웃은 재시도
	 * - 4xx 클라이언트 에러(잘못된 프롬프트, 인증 실패 등)는 재시도도 실패할 것이기 때문에 재시도하지 않음
	 */
	private boolean isRetryable(Throwable t) {
		// 타임아웃이나 네트워크 연결 오류는 재시도
		if (t instanceof TimeoutException || t instanceof WebClientRequestException) {
			return true;
		}

		// AiClientException인 경우 에러 코드 확인
		if (t instanceof AiException e) {
			AiErrorCode code = (AiErrorCode) e.getErrorCode();
			return code == AiErrorCode.AI_SERVER_ERROR || code == AiErrorCode.AI_TIMEOUT;
		}

		// 그 외 4xx 에러 등은 재시도 X
		return false;
	}

	/**
	 * 외부 AI로부터 온 블록 추천 응답을 가공하는 메서드
	 * */
	private Map<Long, Double> parseVlockSuggestionResponse(AiResponseDTO response, List<Vlock> candidates) {
		if (candidates == null || candidates.isEmpty()) {
			return Map.of();
		}

		String raw = (response == null) ? null : response.content();

		if (raw == null || raw.isBlank()) {
			return fallback(candidates);
		}

		try {
			// 코드 블록 및 불필요한 텍스트 제거
			String extractedJson = extractJson(raw);

			// JSON 파싱
			Map<String, Double> parsed = objectMapper.readValue(
					extractedJson,
					new TypeReference<Map<String, Double>>() {}
			);

			return candidates.stream()
					.collect(Collectors.toMap(
							Vlock::getId,
							vlock -> {
								Double score = parsed.get(String.valueOf(vlock.getId()));
								if (score == null) {
									score = 0.5;  // 누락된 후보에는 기본 점수 제공
								}

								return Math.max(0.0, Math.min(1.0, score));  // 점수를 0~1 사이로 보정
							},
							(existing, replacement) -> existing  // 중복된 key 발생 시 기존값 유지
					));
		} catch (Exception e) {
			log.error("AI 응답 파싱 실패: {}", e.getMessage(), e);
			return fallback(candidates);
		}
	}

	/**
	 * AI 응답에서 JSON을 추출하는 메서드
	 * */
	private String extractJson(String raw) {
		String cleaned = raw.replaceAll("(?i)```(json)?", "").replaceAll("```", "").trim();  // 마크다운 코드 블록 제거 및 대소문자 무시

		int start = cleaned.indexOf('{');
		int end = cleaned.lastIndexOf('}');

		if (start == -1 || end == -1 || start >= end) {
			throw new AiException(AiErrorCode.AI_PARSE_ERROR);
		}

		return cleaned.substring(start, end + 1);
	}

	/**
	 * AI 응답 파싱 등 실패시 기본값으로 블록 추천 점수를 보정해주는 메서드
	 * */
	private Map<Long, Double> fallback(List<Vlock> candidates) {
		Map<Long, Double> fallback = new HashMap<>();
		for (Vlock vlock : candidates) {
			fallback.put(vlock.getId(), 0.5);
		}
		return fallback;
	}

	/**
	 * 외부 AI로부터 온 태그 생성 응답을 가공하는 메서드
	 * */
	private AiTagResponseDTO parseTagResponse(AiResponseDTO response) {
		String raw = (response == null) ? null : response.content();

		if (raw ==  null || raw.isBlank()) {
			log.error("AI 태그 생성 응답이 비어있습니다.");
			throw new AiException(AiErrorCode.AI_SERVER_ERROR);
		}

		try {
			String extractedJson = extractJson(raw);
			return objectMapper.readValue(extractedJson, AiTagResponseDTO.class);
		} catch (JsonProcessingException e) {
			log.error("AI 태그 생성 응답 파싱 실패: {}", e.getMessage(), e);
			throw new AiException(AiErrorCode.AI_PARSE_ERROR);
		} catch (Exception e) {
			throw new AiException(AiErrorCode.AI_SERVER_ERROR);
		}
	}
}
