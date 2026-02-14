package org.umc.travlocksserver.infra.redis.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class TemplateRecommendationCache {

	private final RedisTemplate<String, Object> redis;
	private final ObjectMapper objectMapper;

	@Value("${cache.recommendation.templates.cache-ttl-minutes}")
	private long ttlMinutes;

	public CachedTemplateRecommendations get(Long memberId) {
		Object object = redis.opsForValue().get(key(memberId));
		if (object == null) {
			return null;
		}
		return objectMapper.convertValue(object, CachedTemplateRecommendations.class);
	}

	public void set(Long memberId, CachedTemplateRecommendations value) {
		redis.opsForValue().set(key(memberId), value, Duration.ofMinutes(ttlMinutes));
	}

	private String key(Long memberId) {
		return "recommendation:templates:v1:member:" + memberId;
	}
}
