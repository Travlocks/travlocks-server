package org.umc.travlocksserver.domain.member.exception;

import org.umc.travlocksserver.global.code.BaseCode;
import org.umc.travlocksserver.global.exception.GeneralException;

public class MemberException extends GeneralException {
    public MemberException(BaseCode errorCode) { super(errorCode); }
}
