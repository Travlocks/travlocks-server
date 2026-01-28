package org.umc.travlocksserver.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// ✨ Spring에서 Redis에 저장되는 값을 사람이 읽을 수 있는 JSON으로 저장하도록 만드는 클래스
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        ObjectMapper mapper = objectMapper.copy()  // Redis 전용 복사본을 만들어서 Redis 직렬화 옵션을 바꿔도 API JSON 응답에는 영향주지 않음
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        StringRedisSerializer string = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer json = new GenericJackson2JsonRedisSerializer(mapper);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(string);  // Redis key를 사람이 읽는 문자열로 저장
        template.setValueSerializer(json);  // Redis value를 JSON 문자열로 저장

        template.setHashKeySerializer(string);
        template.setHashValueSerializer(json);

        template.setDefaultSerializer(json);

        template.afterPropertiesSet();

        return template;
    }
}
