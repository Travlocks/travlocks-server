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
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // JWT 붙이기 전까지는 전부 허용
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}