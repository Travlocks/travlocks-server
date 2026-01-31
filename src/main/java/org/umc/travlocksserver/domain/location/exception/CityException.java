package org.umc.travlocksserver.domain.location.exception;

import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class CityException extends GeneralException {
	public CityException(BaseCode errorCode) {
		super(errorCode);
	}
}
