package org.umc.travlocksserver.global.apiPayload.exception;

import lombok.Getter;
import org.umc.travlocksserver.global.apiPayload.code.BaseCode;

@Getter
public abstract class GeneralException extends RuntimeException {

    private final BaseCode errorCode;

    public GeneralException(BaseCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
