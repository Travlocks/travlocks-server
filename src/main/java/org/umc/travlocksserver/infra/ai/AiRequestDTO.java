package org.umc.travlocksserver.infra.ai;

import java.util.List;

// ✨ 외부 AI인 HyperClova에게 요청하는 DTO
public record AiRequestDTO(
        List<Message> messages
) {
    public record Message(String role, String content) {

        public static Message system(String text) {
            return new Message("system", text);
        }

        public static Message user(String text) {
            return new Message("user", text);
        }
    }

    public static AiRequestDTO score(String prompt) {
        return new AiRequestDTO(
                List.of(
                        Message.system(
                                "너는 여행 장소 추천을 돕는 AI다. 아래 JSON의 구조를 읽고, " +
                                        "candidates 각각이 '지금 이 일정에 하나 더 넣기 좋은지'를 0~1 점수로 평가해라. " +
                                        "반드시 {\"<id>\": <score>} 형식의 JSON만 출력해라."
                        ),
                        Message.user(prompt)
                )
        );
    }

    public static AiRequestDTO generateTag(String prompt) {
        return new AiRequestDTO(
                List.of(
                        Message.system(
                                """
                                        너는 여행 템플릿에 태그를 생성해주는 AI다. 아래 규칙을 기반으로 태그를 생성해라.
                                        
                                        [출력 규칙]
                                        - 반드시 JSON만 아래 형식으로 출력한다.
                                          {
                                            "city": ["..."], // 0~2개
                                            "free": ["..."]  // 정확히 2개
                                          }
                                          
                                        [세부 지역 태그(city) 정책]  
                                        - 생성 개수: 0~2개
                                        - 생성 기준: cityCandidates에 있는 후보 제외하고 생성 (적절한 태그가 없다면 생성하지 않는다.)
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
                                        """
                        ),
                        Message.user(prompt)
                )
        );
    }
}
