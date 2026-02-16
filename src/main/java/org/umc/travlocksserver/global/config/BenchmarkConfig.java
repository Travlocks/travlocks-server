package org.umc.travlocksserver.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.umc.travlocksserver.infra.aop.AiCallCounter;

@Configuration
@Profile("benchmark")
public class BenchmarkConfig {

    @Bean
    public AiCallCounter aiCallCounter() {
        return new AiCallCounter();
    }
}
