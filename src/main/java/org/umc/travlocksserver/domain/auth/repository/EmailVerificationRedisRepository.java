package org.umc.travlocksserver.domain.auth.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public record EmailVerificationCache(
            String email,
            String code
    ) {}

    public void save(String verificationId, EmailVerificationCache cache, Duration ttl) {
        try {
            String value = objectMapper.writeValueAsString(cache); // Java 객체 → JSON 문자열
            redisTemplate.opsForValue().set(key(verificationId), value, ttl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Redis 직렬화 실패", e);
        }
    }

    public String key(String verificationId) {
        return "email_verification:" + verificationId;
    }
}
