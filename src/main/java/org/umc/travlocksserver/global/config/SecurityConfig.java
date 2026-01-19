package org.umc.travlocksserver.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API 서버 → 폼 로그인, 기본 로그인 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

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
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}