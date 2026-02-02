package org.umc.travlocksserver.infra.ai;

import java.util.List;

// ✨ 외부 AI인 HyperClova에게 요청하는 DTO
public record HyperClovaRequestDTO(
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

    public static HyperClovaRequestDTO score(String prompt) {
        return new HyperClovaRequestDTO(
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
}
