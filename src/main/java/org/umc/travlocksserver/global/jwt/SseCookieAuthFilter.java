package org.umc.travlocksserver.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SseCookieAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // Sse 구독 요청만 필터 적용
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/notifications/subscribe");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 이미 JWT 인증된 경우 SSE 쿠키 인증없이 패스 (방어용)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // SSE 쿠키 추출
        String token = extractCookie(request, "SSE_TOKEN");

        // 쿠키 추출한 경우 검증
        if (token != null) {
            try {
                Long memberId = jwtTokenProvider.extractMemberIdFromSseToken(token);

                // Security 인증 완료 상태 객체 생성
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        memberId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception ignored) {
                // invalid면 통과 -> Security에서 401 처리
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 쿠키 배열에서 특정 쿠키를 찾는 메서드
     * */
    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
