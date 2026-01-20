package org.umc.travlocksserver.domain.auth.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.exception.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.repository.SignupTokenRedisRepository;

@Service
@RequiredArgsConstructor
public class SignupTokenService {

    private final SignupTokenRedisRepository signupTokenRedisRepository;

    public String getEmail(String signupToken) {
        String email = signupTokenRedisRepository.findEmail(signupToken);
        if (email == null) throw new AuthException(AuthErrorCode.INVALID_SIGNUP_TOKEN);
        return email;
    }

    public void consume(String signupToken) {
        signupTokenRedisRepository.delete(signupToken);
    }
}