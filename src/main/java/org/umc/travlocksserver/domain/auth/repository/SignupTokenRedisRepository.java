package org.umc.travlocksserver.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class SignupTokenRedisRepository {

    private final StringRedisTemplate redisTemplate;

    public void save(String signupToken, String email, Duration ttl) {
        redisTemplate.opsForValue().set(key(signupToken), email, ttl);
    }

    public String findEmail(String signupToken) {
        return redisTemplate.opsForValue().get(key(signupToken));
    }

    public void delete(String signupToken) {
        redisTemplate.delete(key(signupToken));
    }

    private String key(String signupToken) {
        return "signup_token:" + signupToken;
    }
}
