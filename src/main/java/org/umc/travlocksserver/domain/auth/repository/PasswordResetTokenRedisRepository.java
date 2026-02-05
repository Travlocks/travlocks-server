package org.umc.travlocksserver.domain.auth.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public record PasswordResetCache(String email) {}

    public void save(String resetToken, PasswordResetCache cache, Duration ttl) {
        try {
            String value = objectMapper.writeValueAsString(cache);
            redisTemplate.opsForValue().set(key(resetToken), value, ttl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Redis 직렬화 실패", e);
        }
    }

    public PasswordResetCache find(String resetToken) {
        String value = redisTemplate.opsForValue().get(key(resetToken));
        if (value == null) return null;

        try {
            return objectMapper.readValue(value, PasswordResetCache.class);
        } catch (Exception e) {
            throw new IllegalStateException("Redis 역직렬화 실패", e);
        }
    }

    public void delete(String resetToken) {
        redisTemplate.delete(key(resetToken));
    }

    public String key(String resetToken) {
        return "password_reset:" + resetToken;
    }
}
