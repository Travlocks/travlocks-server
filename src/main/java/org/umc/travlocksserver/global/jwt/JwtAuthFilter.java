package org.umc.travlocksserver.global.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.umc.travlocksserver.global.security.code.SecurityErrorCode;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    // request attribute key
    public static final String AUTH_ERROR_ATTR = "AUTH_ERROR";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Authorization 헤더 없거나 Bearer 아니면 통과
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 제거, 토큰만 추출
        String accessToken = authHeader.substring(7);

        try {
            // accessToken 검중
            jwtTokenProvider.validateAccessTokenOrThrow(accessToken);

            Long memberId = jwtTokenProvider.extractMemberIdFromAccessToken(accessToken);
            if (memberId == null) {
                request.setAttribute(AUTH_ERROR_ATTR, SecurityErrorCode.INVALID_ACCESS_TOKEN);
                SecurityContextHolder.clearContext();
                throw new BadCredentialsException("Invalid access token");

            }

            // Spring Security 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            memberId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            // SecurityContext에 인증 정보 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            // access 만료
            request.setAttribute(AUTH_ERROR_ATTR, SecurityErrorCode.ACCESS_TOKEN_EXPIRED);
            SecurityContextHolder.clearContext();
            throw new CredentialsExpiredException("Access token expired", e);

        } catch (JwtException | IllegalArgumentException e) {
            // 서명불일치/위조/형식 오류 등
            request.setAttribute(AUTH_ERROR_ATTR, SecurityErrorCode.INVALID_ACCESS_TOKEN);
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException("Invalid access token", e);
        }

    }
}
