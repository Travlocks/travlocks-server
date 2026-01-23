package org.umc.travlocksserver.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.umc.travlocksserver.global.jwt.JwtAuthFilter;
import org.umc.travlocksserver.global.response.ErrorResponse;
import org.umc.travlocksserver.global.security.code.SecurityErrorCode;

import java.io.IOException;

/**
 * 인증 실패(401) 발생 시 호출되는 EntryPoint
 *
 * Spring Security 필터 체인에서 AuthenticationException이 발생하면,
 * ExceptionTranslationFilter가 이를 가로채고,
 * 등록된 AuthenticationEntryPoint의 commence() 메서드를 호출한다.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        // JwtAuthFilter에서 인증 실패 시 request에 저장한 에러 코드 조회
        Object attr = request.getAttribute(JwtAuthFilter.AUTH_ERROR_ATTR);

        // request attribute에 SecurityErrorCode가 담겨 있으면 그대로 사용
        SecurityErrorCode errorCode =
                (attr instanceof SecurityErrorCode sec)
                        ? sec
                        : SecurityErrorCode.UNAUTHORIZED;

        // ErrorResponse 형식으로 401 응답 반환
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(errorCode)));
    }
}






