package org.umc.travlocksserver.domain.favorite.exception;

import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class FavoriteException extends GeneralException {

    public FavoriteException(BaseCode errorCode) {
        super(errorCode);
    }
}