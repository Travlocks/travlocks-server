package org.umc.travlocksserver.domain.vlock.exception;

import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class VlockException extends GeneralException {
	public VlockException(BaseCode errorCode) {
		super(errorCode);
	}
}
