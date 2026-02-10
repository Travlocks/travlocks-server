package org.umc.travlocksserver.global.exception.handler;

import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class S3ExceptionHandler extends GeneralException {
	public S3ExceptionHandler(BaseCode errorCode) {
		super(errorCode);
	}
}
