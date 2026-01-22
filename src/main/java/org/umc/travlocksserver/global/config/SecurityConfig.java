package org.umc.travlocksserver.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.umc.travlocksserver.global.jwt.JwtAuthFilter;
import org.umc.travlocksserver.global.security.CustomAuthEntryPoint;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API 서버 → 폼 로그인, 기본 로그인 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                //  인증 실패(401) 응답을 커스터마이징
                .exceptionHandling(e -> e.authenticationEntryPoint(customAuthEntryPoint))

                // 인가 설정
                .authorizeHttpRequests(auth -> auth
                        // Swagger 관련 URL 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/v1/auth/email-verification",
                                "/api/v1/auth/email-verification/confirm",
                                "/api/v1/auth/email-verification/resend",
                                "/api/v1/members/email/exists",
                                "/api/v1/members/nickname/exists",
                                "/api/v1/members/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh"
                        ).permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                // 액세스 토큰 검증 필터
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}