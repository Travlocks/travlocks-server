package org.umc.travlocksserver.infra.ai.dto;

import java.util.List;

// ✨ 외부 AI인 HyperClova에게 요청하는 DTO
public record AiRequestDTO(List<Message> messages) {

	public record Message(String role, String content) { }

	public static AiRequestDTO of(String systemPrompt, String userPrompt) {
		return new AiRequestDTO(List.of(
				new Message("system", systemPrompt),
				new Message("user", userPrompt)
		));
	}
}
