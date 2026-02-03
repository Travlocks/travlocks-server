package org.umc.travlocksserver.domain.template.exception;

import org.umc.travlocksserver.domain.template.exception.code.TemplateDayErrorCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class TemplateDayException extends GeneralException {
    public TemplateDayException(TemplateDayErrorCode errorCode) {
        super(errorCode);
    }
}
