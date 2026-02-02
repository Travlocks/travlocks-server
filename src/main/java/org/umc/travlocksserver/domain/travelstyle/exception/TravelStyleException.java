package org.umc.travlocksserver.domain.travelstyle.exception;

import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class TravelStyleException extends GeneralException {
    public TravelStyleException(BaseCode errorCode) { super(errorCode); }
}
