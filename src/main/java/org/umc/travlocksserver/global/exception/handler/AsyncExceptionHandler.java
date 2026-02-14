package org.umc.travlocksserver.global.exception.handler;

import java.lang.reflect.Method;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	@Override
	public void handleUncaughtException(@NonNull
	Throwable ex, @NonNull
	Method method, @Nullable
	Object... params) {
		log.error("[Async Error] Method: {}, Message: {}",
			method.getName(), ex.getMessage(), ex);
	}
}
