package org.umc.travlocksserver.global.code;

import org.springframework.http.HttpStatus;

public interface BaseCode {

    HttpStatus getStatus();
    String getMessage();

    default String getCode() {
        return ((Enum<?>) this).name();
    }
}
