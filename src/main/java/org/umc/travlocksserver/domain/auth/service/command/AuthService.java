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
import org.umc.travlocksserver.domain.auth.exception.code.AuthErrorCode;
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
        // 회원 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
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

        // 1) 쿠키에서 refreshToken 꺼내기
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        // 2) refreshToken 서명/만료 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 3) refreshToken에서 jti 추출
        String jti = jwtTokenProvider.extractJti(refreshToken);
        if (jti == null || jti.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 4) Redis에 jti 존재하는지 확인 (존재하면 memberId 얻음)
        Long memberIdFromRedis = refreshTokenRedisRepository.findMemberId(jti);
        if (memberIdFromRedis == null) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 5) refreshToken 안의 sub(memberId)와 Redis 값 일치 검증
        Long memberIdFromToken = jwtTokenProvider.extractMemberId(refreshToken);
        if (memberIdFromToken == null || !memberIdFromToken.equals(memberIdFromRedis)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 6) 새 accessToken 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(memberIdFromRedis);

        return new AuthRefreshResponseDTO(
                newAccessToken,
                jwtTokenProvider.getAccessTokenExpiresInSeconds()
        );
    }

    // AccessToken, RefreshToken 발급
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
                .secure(false) // 운영환경에선 true로 변경
                .path("/")
                .maxAge(refreshTtl)
                .sameSite("Lax") // 운영환경에선 None으로 변경
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new IssuedTokens(accessToken, jwtTokenProvider.getAccessTokenExpiresInSeconds());
    }

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
