package org.umc.travlocksserver.infra.redis.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TemplateTagGenerationQueue {

    private static final String KEY = "tag-generation";

    @Value("${tag.delay-minutes}")
    private Long delayMinutes;

    private final RedisTemplate<String, Object> redis;

    /**
     * AI 태그 생성 작업을 예약하는 메서드
     * <p>
     * Redis Sorted Set
     * - member: templateId
     * - score: AI 실행 예정 시각 (수정 시간 + 20분)
     * <p>
     * 동일한 templateID를 다시 추가하면 새로운 데이터가 생성되는 것이 아니라 score(실행 예정 시간)만 갱신된다.
    * */
    public void enqueue(Long templateId) {
        log.info("큐에 들어감 {}", templateId);
        Instant scheduledAt = Instant.now().plus(Duration.ofMinutes(delayMinutes));
        redis.opsForZSet().add(
                KEY, templateId, scheduledAt.toEpochMilli()
        );
    }

    /**
     * 실행 예정 시간이 지난 템플릿 ID들을 조회하는 메서드
    * */
    public Set<Long> findDueTemplateIds() {
        Set<Object> values = redis.opsForZSet()
                .rangeByScore(
                        KEY, 0 ,Instant.now().toEpochMilli()
                );

        if (values == null || values.isEmpty()) {
            return Set.of();
        }

        return values.stream()
                .map(value -> ((Number) value).longValue())
                .collect(Collectors.toSet());
    }

    /**
     * AI 태그 생성이 완료된 예약을 삭제하는 메서드
    * */
    public void remove(Long templateId) {
        redis.opsForZSet().remove(KEY, templateId);
    }
}
