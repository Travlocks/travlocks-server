package org.umc.travlocksserver.global.apiPayload.exception;

import org.umc.travlocksserver.global.apiPayload.code.common.CommonErrorCode;

public class MemberException extends GeneralException {

    // MemberErrorCode 생성하면 아래 CommonErrorCode -> MemberErrorCode로 변경
    public MemberException(CommonErrorCode code) {
        super(code);
    }
}
