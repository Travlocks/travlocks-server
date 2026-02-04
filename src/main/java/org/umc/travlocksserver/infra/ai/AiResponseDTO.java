package org.umc.travlocksserver.infra.ai;

// ✨ 외부 AI인 HyperClova으로 응답받는 DTO
public record AiResponseDTO(
        Result result
) {
    public record Result(
            Message message
    ) {
        public record Message(String role, String content) {}
    }

    public String content() {
        return result != null && result.message != null
                ? result.message.content
                : null;
    }
}
