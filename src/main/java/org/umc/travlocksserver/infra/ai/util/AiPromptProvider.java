package org.umc.travlocksserver.infra.ai.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.infra.ai.exception.AiErrorCode;
import org.umc.travlocksserver.infra.ai.exception.AiException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiPromptProvider {

    private final ObjectMapper objectMapper;

    private static final String VLOCK_SUGGESTION_SCORING_SYSTEM_PROMPT = """
		너는 여행 장소 추천을 돕는 AI다.
		아래 JSON의 구조를 읽고,
		candidates 각각이 '지금 이 일정에 하나 더 넣기 좋은지'를 0~1 점수로 평가해라.
		used에 있는 장소는 결과에 포함하지 않는다.
		반드시 {\\"<id>\\": <score>} 형식의 JSON만 출력해라.
	""";

    private static final String TAG_GENERATION_SYSTEM_PROMPT = """
		너는 여행 템플릿에 태그를 생성해주는 AI다. 아래 규칙을 기반으로 태그를 생성해라.
		[출력 규칙]
		- 반드시 JSON만 아래 형식으로 출력한다.
		{
		    "city": ["..."], // 0~2개
			"free": ["..."]  // 정확히 2개
		}

		[세부 지역 태그(city) 정책]
		- 생성 개수: 0~2개
		- 생성 기준: cityCandidates에 있는 후보 제외하고 생성 (적절한 태그가 없다면 생성하지 않으며, 빈 배열로 반환한다.)
		- 생성 조건: 아래 중 하나라도 충족할 때만 선택 가능
		    1) 전체 장소 중 50% 이상이 동일 세부 지역에 포함
			2) 동선 상 특정 권역에 명확히 집중

		[AI 자유 태그(free)]
		- 생성 개수: 정확히 2개 (초과/미만 불가)
		- 형식: 2~8글자, 명사 또는 짧은 형용사 (띄어쓰기없이)
		- 금지:
		    1) 주관적/개인적 표현
			2) 욕설/비속어/은어
			3) 브랜드/장소명 직접 노출
			4) 태그 간 의미 충돌 (동일한 의미 태그는 1개만 허용)
			5) region, fixedTags와 의미 중복 금지
	""";

    public String buildUserPromptForVlockSuggestion(List<Vlock> usedVlocksInTemplate, List<Vlock> candidates) {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("used", usedVlocksInTemplate.stream()
                .map(vlock -> {
                    Map<String, Object> info = new LinkedHashMap<>(mapVlockInfo(vlock));
                    info.put("stayHours", vlock.getVlockCategory().getStayHours());
                    return info;
                }).toList());

        payload.put("candidates", candidates.stream()
                .map(vlock -> {
                    Map<String, Object> info = new LinkedHashMap<>(mapVlockInfo(vlock));
                    info.put("stayHours", vlock.getVlockCategory().getStayHours());
                    return info;
                }).toList());

        payload.put("criteria", List.of(
                "일정 흐름을 깨지 않는가",
                "무난하게 끼워 넣기 좋은가",
                "맥락상 어색하지 않은가",
                "무리한 일정은 아닌가?"));

        return toJson(payload);
    }

    public String buildUserPromptForTagGeneration(
            String region,
            List<String> fixedTags,
            List<String> cityCandidates,
            List<Vlock> vlocksInTemplate) {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("region", region);
        payload.put("fixedTags", fixedTags);
        payload.put("cityCandidates", cityCandidates);
        payload.put("vlocks", vlocksInTemplate.stream()
                .map(vlock -> {
                    Map<String, Object> info = new LinkedHashMap<>(mapVlockInfo(vlock));
                    info.put("address", vlock.getAddress());
                    return info;
                }).toList());

        return toJson(payload);
    }

    private Map<String, Object> mapVlockInfo(Vlock vlock) {
        return Map.of(
                "id", vlock.getId(),
                "name", vlock.getName(),
                "category", vlock.getVlockCategory().getName()
        );
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object); // JSON 문자열로 변경
        } catch (JsonProcessingException e) {
            log.error("AI 프롬프트 JSON 파싱 실패: {}", e.getMessage(), e);
            throw new AiException(AiErrorCode.AI_PARSE_ERROR);
        }
        catch (Exception e) {
            log.error("AI 프롬프트 파싱 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
            throw new AiException(AiErrorCode.AI_SERVER_ERROR);
        }
    }

    public String getSystemPromptForVlockSuggestion() {
        return VLOCK_SUGGESTION_SCORING_SYSTEM_PROMPT;
    }

    public String getSystemPromptForTagGeneration() {
        return TAG_GENERATION_SYSTEM_PROMPT;
    }
}
