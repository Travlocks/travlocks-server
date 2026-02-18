package org.umc.travlocksserver.infra.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Profile("benchmark")
public class AiCallMetricsAspect {

    private final AiCallCounter aiCallCounter;

    @Around("execution(* org.umc.travlocksserver.infra.ai.client.HyperClovaSuggesionClientBenchmark.generateTags(..))")
    public Object countAiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        aiCallCounter.increment();
        return joinPoint.proceed();
    }
}
