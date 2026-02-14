package org.umc.travlocksserver.domain.vlock.exception;

import org.umc.travlocksserver.domain.vlock.code.VlockCategoryErrorCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class VlockCategoryException extends GeneralException {
	public VlockCategoryException(VlockCategoryErrorCode errorCode) {
		super(errorCode);
	}
}
