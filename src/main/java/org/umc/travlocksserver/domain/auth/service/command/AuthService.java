package org.umc.travlocksserver.domain.auth.service.command;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.auth.dto.request.AuthLoginRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthLoginResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthRefreshResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.repository.RefreshTokenRedisRepository;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.global.jwt.JwtTokenProvider;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${jwt.refresh-ttl-seconds}")
    private long refreshTtlSeconds;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public record IssuedTokens(String accessToken, long accessTokenExpiresIn) {
    }

    public AuthLoginResponseDTO login(AuthLoginRequestDTO request, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!member.matchesPassword(passwordEncoder, request.password())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        IssuedTokens tokens = issueTokens(member.getId(), response);

        return new AuthLoginResponseDTO(
                member.getId(),
                tokens.accessToken(),
                tokens.accessTokenExpiresIn()
        );
    }

    public AuthRefreshResponseDTO refreshAccessToken(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // refreshToken에서 jti 추출
        String jti = jwtTokenProvider.extractJti(refreshToken);
        if (jti == null || jti.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Redis에 jti 존재하는지 확인 (존재하면 memberId 얻음)
        Long memberIdFromRedis = refreshTokenRedisRepository.findMemberId(jti);
        if (memberIdFromRedis == null) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // refreshToken 안의 sub(memberId)와 Redis 값 일치 검증
        Long memberIdFromToken = jwtTokenProvider.extractMemberId(refreshToken);
        if (memberIdFromToken == null || !memberIdFromToken.equals(memberIdFromRedis)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 새 accessToken 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(memberIdFromRedis);

        return new AuthRefreshResponseDTO(
                newAccessToken,
                jwtTokenProvider.getAccessTokenExpiresInSeconds()
        );
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        invalidateRefreshToken(request, response);
    }

    /**
     * AccessToken, RefreshToken 발급
     */
    public IssuedTokens issueTokens(Long memberId, HttpServletResponse response) {
        Duration refreshTtl = Duration.ofSeconds(refreshTtlSeconds);

        // AccessToken
        String accessToken = jwtTokenProvider.generateAccessToken(memberId);

        // RefreshToken (jti 포함)
        String jti = UUID.randomUUID().toString();
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId, jti);

        // refreshToken -> Redis 저장
        refreshTokenRedisRepository.save(jti, memberId, refreshTtl);

        // refreshToken -> Set-Cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .domain(".travlocks.com")
                .maxAge(refreshTtl)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new IssuedTokens(accessToken, jwtTokenProvider.getAccessTokenExpiresInSeconds());
    }

    /**
     * RefreshToken 무효화
     */
    public void invalidateRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
                    String jti = jwtTokenProvider.extractJti(refreshToken);
                    if (jti != null && !jti.isBlank()) {
                        refreshTokenRedisRepository.delete(jti);
                    }
                }
            } catch (Exception ignored) {
                // 멱등 처리: 파싱/검증 오류여도 쿠키 삭제는 수행
            }
        }

        // 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .domain(".travlocks.com")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }


    /**
     * refreshToken 추출
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
