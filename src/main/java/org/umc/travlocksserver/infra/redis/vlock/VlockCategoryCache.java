package org.umc.travlocksserver.infra.redis.vlock;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class VlockCategoryCache {

	private final RedisTemplate<String, Object> redis;
	private final ObjectMapper objectMapper;

	@Value("${cache.categories.cache-ttl-minutes}")
	private long ttlMinutes;

	private static final String KEY = "vlock:categories:v1:all";

	public CachedVlockCategoryList getAll() {
		Object object = redis.opsForValue().get(KEY);

		if (object == null) {
			return null;
		}

		return objectMapper.convertValue(object, CachedVlockCategoryList.class);
	}

	public void setAll(CachedVlockCategoryList value) {
		redis.opsForValue().set(KEY, value, Duration.ofMinutes(ttlMinutes));
	}
}
