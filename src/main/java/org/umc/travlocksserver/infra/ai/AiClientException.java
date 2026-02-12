package org.umc.travlocksserver.infra.ai;

// ✨ 외부 AI 호출시 발생하는 예외
public class AiClientException extends RuntimeException {
	public AiClientException(String message) {
		super(message);
	}
}
