package org.umc.travlocksserver.global.exception;

import org.umc.travlocksserver.global.code.ErrorCode;

public class MemberException extends GeneralException {

    // MemberErrorCode 생성하면 아래 CommonErrorCode -> MemberErrorCode로 변경
    public MemberException(ErrorCode code) {
        super(code);
    }
}
