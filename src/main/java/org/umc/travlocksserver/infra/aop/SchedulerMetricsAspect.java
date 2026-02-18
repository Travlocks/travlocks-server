package org.umc.travlocksserver.infra.aop;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.umc.travlocksserver.infra.ai.entity.AiTagExecutionLog;
import org.umc.travlocksserver.infra.ai.repository.AiTagExecutionLogRepository;

@Aspect
@Component
@Profile("benchmark")
@RequiredArgsConstructor
public class SchedulerMetricsAspect {

    private final EntityManager entityManager;
    private final AiCallCounter aiCallCounter;
    private final AiTagExecutionLogRepository aiTagExecutionLogRepository;

    @Around("execution(* org.umc.travlocksserver.infra.scheduler.TemplateTagScheduler.run())")
    public Object measureScheduler(ProceedingJoinPoint joinPoint) throws Throwable {

        // DB 통계을 위해 Hibernate Statistics 활성화
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // AI 호출 카운터 초기화
        aiCallCounter.reset();

        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        int templateCount = (int) result;
        long end = System.currentTimeMillis();
        long queryCount = stats.getPrepareStatementCount();
        long entityFetchCount = stats.getEntityFetchCount();
        int aiCallCount = aiCallCounter.get();

        AiTagExecutionLog log = AiTagExecutionLog.create(
                start,
                end,
                end - start,
                templateCount,
                aiCallCount,
                (end - start) / templateCount,
                queryCount,
                entityFetchCount
        );

        aiTagExecutionLogRepository.save(log);

        return result;
    }
}
