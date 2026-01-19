package org.umc.travlocksserver.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private static final String JTI_CLAIM = "jti";

    public JwtTokenProviderImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-ttl-seconds:3600}") long accessTtlSeconds,
            @Value("${jwt.refresh-ttl-seconds:1209600}") long refreshTtlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    @Override
    public String generateAccessToken(Long memberId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    @Override
    public String generateRefreshToken(Long memberId, String jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshTtlSeconds);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(JTI_CLAIM, jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    @Override
    public long getAccessTokenExpiresInSeconds() {
        return accessTtlSeconds;
    }

    @Override
    public String extractJti(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        Object jti = claims.get(JTI_CLAIM);
        return jti == null ? null : jti.toString();
    }

    @Override
    public Long extractMemberId(String refreshToken) {
        Claims claims = parseClaims(refreshToken);

        String sub = claims.getSubject();
        if (sub == null || sub.isBlank()) return null;

        try {
            return Long.parseLong(sub);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        try {
            parseClaims(refreshToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // JWT의 서명과 만료(exp) 여부를 검증한 뒤, payload에 담긴 Claims를 반환
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
