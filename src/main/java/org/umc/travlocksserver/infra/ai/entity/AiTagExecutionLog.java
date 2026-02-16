package org.umc.travlocksserver.infra.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiTagExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_tag_execution_log_id")
    private Long id;

    private Long start;

    private Long end;

    private Long totalProcessingTimeMs;

    private Integer templateCount;

    private Integer aiCallCount;

    private Long avgProcessingTimeMs;

    private Long queryCount;

    private Long fetchCount;

    public AiTagExecutionLog(Long start, Long end, Long totalProcessingTimeMs, Integer templateCount, Integer aiCallCount, Long avgProcessingTimeMs, Long queryCount, Long fetchCount) {
        this.start =  start;
        this.end =  end;
        this.totalProcessingTimeMs = totalProcessingTimeMs;
        this.templateCount = templateCount;
        this.aiCallCount = aiCallCount;
        this.avgProcessingTimeMs = avgProcessingTimeMs;
        this.queryCount = queryCount;
        this.fetchCount = fetchCount;
    }

    public static AiTagExecutionLog create(
            Long start,
            Long end,
            Long totalProcessingTimeMs,
            Integer templateCount,
            Integer aiCallCount,
            Long avgProcessingTimeMs,
            Long queryCount,
            Long fetchCount
    ) {
        return new AiTagExecutionLog(start, end, totalProcessingTimeMs, templateCount, aiCallCount, avgProcessingTimeMs,  queryCount, fetchCount);
    }
}
