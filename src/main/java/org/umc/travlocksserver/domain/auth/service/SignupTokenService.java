package org.umc.travlocksserver.domain.auth.service;

public interface SignupTokenService {

    /**
     * signupToken이 유효하면 email을 반환하고,
     * 1회성 토큰이므로 즉시 소모(삭제)한다.
     */
    String consumeAndGetEmail(String signupToken);
}
