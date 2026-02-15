package org.umc.travlocksserver.infra.ai;

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

    private Long totalAiProcessingTimeMs;

    private Long avgAiProcessingTimeMs;

    public AiTagExecutionLog(Long start, Long end, Long totalProcessingTimeMs, Integer templateCount, Integer aiCallCount, Long totalAiProcessingTimeMs, Long avgAiProcessingTimeMs) {
        this.start =  start;
        this.end =  end;
        this.totalProcessingTimeMs = totalAiProcessingTimeMs;
        this.templateCount = templateCount;
        this.aiCallCount = aiCallCount;
        this.totalAiProcessingTimeMs = totalAiProcessingTimeMs;
        this.avgAiProcessingTimeMs = avgAiProcessingTimeMs;
    }

    public static AiTagExecutionLog create(
            Long start,
            Long end,
            Long totalProcessingTimeMs,
            Integer templateCount,
            Integer aiCallCount,
            Long totalAiProcessingTimeMs,
            Long avgAiProcessingTimeMs
    ) {
        return new AiTagExecutionLog(start, end, totalProcessingTimeMs, templateCount, aiCallCount, totalAiProcessingTimeMs, avgAiProcessingTimeMs);
    }
}
