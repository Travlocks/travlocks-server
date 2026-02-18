package org.umc.travlocksserver.infra.ai.exception;

import org.umc.travlocksserver.global.exception.GeneralException;

// ✨ 외부 AI 호출시 발생하는 예외
public class AiException extends GeneralException {
	public AiException(AiErrorCode code) {
		super(code);
	}
}
