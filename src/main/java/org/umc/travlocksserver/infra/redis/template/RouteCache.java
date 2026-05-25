package org.umc.travlocksserver.infra.redis.template;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDayRouteResponseDTO;
import org.umc.travlocksserver.domain.template.enums.TransportType;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RouteCache {

	private final RedisTemplate<String, Object> redis;
	private final ObjectMapper objectMapper;

	private static final String KEY_PREFIX = "moveTime::";

	public TemplateDayRouteResponseDTO get(Long fromVlockId, Long toVlockId, TransportType transportType) {
		Object object = redis.opsForValue().get(key(fromVlockId, toVlockId, transportType));
		if (object == null) {
			return null;
		}
		return objectMapper.convertValue(object, TemplateDayRouteResponseDTO.class);
	}

	public void set(Long fromVlockId, Long toVlockId, TransportType transportType, TemplateDayRouteResponseDTO value) {
		Duration ttl = switch (transportType) {
			case WALK -> Duration.ofDays(365);
			case CAR -> Duration.ofDays(7);
			case TRANSIT -> Duration.ofDays(1);
		};
		redis.opsForValue().set(key(fromVlockId, toVlockId, transportType), value, ttl);
	}

	public void evict(Long fromVlockId, Long toVlockId, TransportType transportType) {
		redis.delete(key(fromVlockId, toVlockId, transportType));
	}

	private String key(Long fromVlockId, Long toVlockId, TransportType transportType) {
		return KEY_PREFIX + fromVlockId + "_" + toVlockId + "_" + transportType.name();
	}
}
