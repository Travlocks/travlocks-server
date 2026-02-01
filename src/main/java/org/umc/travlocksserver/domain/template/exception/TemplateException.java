package org.umc.travlocksserver.domain.template.exception;

import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class TemplateException extends GeneralException {
	public TemplateException(BaseCode errorCode) {
		super(errorCode);
	}
}