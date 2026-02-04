package org.umc.travlocksserver.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private String key(String jti) {
        return "refresh_token:" + jti;
    }

    public void save(String jti, Long memberId, Duration ttl) {
        redisTemplate.opsForValue().set(key(jti), String.valueOf(memberId), ttl);
    }

    public Long findMemberId(String jti) {
        String value = redisTemplate.opsForValue().get(key(jti));
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void delete(String jti) {
        redisTemplate.delete(key(jti));
    }


}
