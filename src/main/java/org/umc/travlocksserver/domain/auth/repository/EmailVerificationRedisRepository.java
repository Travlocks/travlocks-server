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

    public EmailVerificationCache find(String verificationId) {
        String value = redisTemplate.opsForValue().get(key(verificationId));
        if (value == null) return null;

        try {
            return objectMapper.readValue(value, EmailVerificationCache.class);
        } catch (Exception e) {
            throw new IllegalStateException("Redis 역직렬화 실패", e);
        }
    }

    public void delete(String verificationId) {
        redisTemplate.delete(key(verificationId));
    }

    public String key(String verificationId) {
        return "email_verification:" + verificationId;
    }

}
