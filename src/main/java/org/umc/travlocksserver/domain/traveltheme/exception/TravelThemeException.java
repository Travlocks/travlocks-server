package org.umc.travlocksserver.domain.traveltheme.exception;

import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class TravelThemeException extends GeneralException {
    public TravelThemeException(BaseCode errorCode) { super(errorCode); }
}