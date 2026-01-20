package org.umc.travlocksserver.global.exception;

import lombok.Getter;
import org.umc.travlocksserver.global.code.BaseCode;

@Getter
public abstract class GeneralException extends RuntimeException {

	private final BaseCode errorCode;

	public GeneralException(BaseCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
