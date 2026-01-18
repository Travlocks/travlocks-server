package org.umc.travlocksserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.exception.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.repository.SignupTokenRedisRepository;

@Service
@RequiredArgsConstructor
public class SignupTokenServiceImpl implements SignupTokenService {

    private final SignupTokenRedisRepository signupTokenRedisRepository;

    @Override
    public String consumeAndGetEmail(String signupToken) {

        String email = signupTokenRedisRepository.findEmail(signupToken);

        // 토큰 없음 / 만료
        if (email == null) {
            throw new AuthException(AuthErrorCode.INVALID_SIGNUP_TOKEN);
        }

        // 1회성 → 즉시 삭제
        signupTokenRedisRepository.delete(signupToken);

        return email;
    }
}
