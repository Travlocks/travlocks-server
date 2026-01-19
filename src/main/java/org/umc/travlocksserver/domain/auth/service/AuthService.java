package org.umc.travlocksserver.domain.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.auth.dto.request.AuthLoginRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthLoginResponseDTO;
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

    private static final Duration REFRESH_TTL = Duration.ofDays(14); // 1209600s
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public AuthLoginResponseDTO login(AuthLoginRequestDTO request, HttpServletResponse response) {

        // 1) 회원 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        
        // 3) 토큰 생성
        // AccessToken
        String accessToken = jwtTokenProvider.generateAccessToken(member.getId());

        // RefreshToken (jti 포함)
        String jti = UUID.randomUUID().toString();
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId(), jti);

        // 4) refreshToken -> Redis 저장
        refreshTokenRedisRepository.save(jti, member.getId(), REFRESH_TTL);

        // 5) refreshToken -> Set-Cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // 운영환경에선 true로 변경
                .path("/api/v1/auth/refresh")
                .maxAge(REFRESH_TTL)
                .sameSite("Lax") // 운영환경에선 None으로 변경
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 6) Response Body
        return new AuthLoginResponseDTO(
                member.getId(),
                accessToken,
                jwtTokenProvider.getAccessTokenExpiresInSeconds()
        );
    }

}
