package org.umc.travlocksserver.global.security.cookie;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieFactory {

	@Value("${cookie.sse-token.token-ttl-ms}")
	private long tokenTtlMs;

	@Value("${cookie.sse-token.name}")
	private String sseTokenName;

	@Value("${cookie.sse-token.http-only}")
	private boolean sseTokenHttpOnly;

	@Value("${cookie.sse-token.secure}")
	private boolean sseTokenSecure;

	@Value("${cookie.sse-token.same-site}")
	private String sseTokenSameSite;

	@Value("${cookie.sse-token.path}")
	private String sseTokenPath;

	public ResponseCookie createSseTokenCookie(String sseToken) {
		long ttlSeconds = tokenTtlMs / 1000;
		return ResponseCookie.from(sseTokenName, sseToken)
			.httpOnly(sseTokenHttpOnly)
			.secure(sseTokenSecure)
			.sameSite(sseTokenSameSite)
			.path(sseTokenPath)
			.maxAge(Duration.ofSeconds(ttlSeconds))
			.build();
	}
}
