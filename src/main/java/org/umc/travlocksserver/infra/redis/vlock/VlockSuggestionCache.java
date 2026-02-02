package org.umc.travlocksserver.infra.redis.vlock;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class VlockSuggestionCache {

    private final RedisTemplate<String, Object> redis;
    private final ObjectMapper objectMapper;

    @Value("${cache.recommendation.vlocks.cache-ttl-minutes}")
    private long ttlMinutes;

    public CachedVlockSuggestions get(Long templateDayId) {
        Object object =  redis.opsForValue().get(key(templateDayId));
        if (object == null) {
            return null;
        }
        return objectMapper.convertValue(object, CachedVlockSuggestions.class);
    }

    public void set(Long templateDayId, CachedVlockSuggestions value) {
        redis.opsForValue().set(key(templateDayId), value, Duration.ofMinutes(ttlMinutes));
    }

    public void evict(Long templateDayId) {
        redis.delete(key(templateDayId));
    }

    private String key(Long templateDayId) {
        return "suggestion:vlocks:v1:templateDay:" + templateDayId;
    }
}
