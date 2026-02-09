package org.umc.travlocksserver.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.umc.travlocksserver.global.jwt.JwtAuthFilter;
import org.umc.travlocksserver.global.security.CustomAuthEntryPoint;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // REST API 서버 → 폼 로그인, 기본 로그인 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                //  인증 실패(401) 응답을 커스터마이징
                .exceptionHandling(e -> e.authenticationEntryPoint(customAuthEntryPoint))

                // 인가 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger 관련 URL 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/health"

                        ).permitAll()

                        .requestMatchers(
                                // 회원가입 & 이메일 인증
                                "/api/v1/auth/email-verification",
                                "/api/v1/auth/email-verification/confirm",
                                "/api/v1/auth/email-verification/resend",
                                "/api/v1/members/signup",

                                // 비밀번호 재설정 (로그인 전)
                                "/api/v1/auth/password-reset/request",
                                "/api/v1/auth/password-reset/verify",
                                "/api/v1/auth/password-reset/confirm",

                                // 로그인/토큰
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout",

                                // OAuth 로그인
                                "/api/v1/auth/oauth/**",

                                // 중복/존재 여부 검사
                                "/api/v1/members/email/exists",
                                "/api/v1/members/nickname/exists"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/members/onboarding"
                        ).authenticated()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                // 액세스 토큰 검증 필터
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",

                // legacy (.kro.kr)
                "https://travlocks.kro.kr",
                "https://www.travlocks.kro.kr",
                "https://api.travlocks.kro.kr",

                // new (.com)
                "https://travlocks.com",
                "https://www.travlocks.com",
                "https://api.travlocks.com",

                // Vercel preview
                "https://*.vercel.app"
        ));
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}