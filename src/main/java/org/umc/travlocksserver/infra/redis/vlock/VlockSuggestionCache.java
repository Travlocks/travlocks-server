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

	@Value("${cache.suggestion.vlocks.cache-ttl-minutes}")
	private long ttlMinutes;

	public CachedVlockSuggestions get(Long templateId) {
		Object object = redis.opsForValue().get(key(templateId));
		if (object == null) {
			return null;
		}
		return objectMapper.convertValue(object, CachedVlockSuggestions.class);
	}

	public void set(Long templateId, CachedVlockSuggestions value) {
		redis.opsForValue().set(key(templateId), value, Duration.ofMinutes(ttlMinutes));
	}

	public void evict(Long templateId) {
		redis.delete(key(templateId));
	}

	private String key(Long templateId) {
		return "suggestion:vlocks:v1:template:" + templateId;
	}
}
