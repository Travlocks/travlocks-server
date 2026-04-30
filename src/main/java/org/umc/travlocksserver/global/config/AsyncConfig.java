package org.umc.travlocksserver.global.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.umc.travlocksserver.global.exception.handler.AsyncExceptionHandler;

@Configuration
@EnableAsync
@EnableCaching
public class AsyncConfig implements AsyncConfigurer {

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new AsyncExceptionHandler();
	}

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("moveTime");
	}
}
