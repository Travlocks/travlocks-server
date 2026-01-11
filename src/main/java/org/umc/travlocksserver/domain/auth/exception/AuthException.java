package org.umc.travlocksserver.domain.auth.exception;

import org.umc.travlocksserver.global.apiPayload.code.BaseCode;
import org.umc.travlocksserver.global.apiPayload.exception.GeneralException;

public class AuthException extends GeneralException {
    public AuthException(BaseCode errorCode) {
        super(errorCode);
    }
}

