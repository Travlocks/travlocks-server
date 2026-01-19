package org.umc.travlocksserver.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.umc.travlocksserver.domain.auth.dto.request.AuthLoginRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthResendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthSendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthVerifyEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthLoginResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthRefreshResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthVerifyEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.code.AuthSuccessCode;
import org.umc.travlocksserver.global.response.SuccessResponse;

public interface AuthControllerDocs {

    @Operation(
            summary = "이메일 인증 코드 발송 API",
            description = "회원가입을 위한 이메일 인증 코드를 발송합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 코드 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<AuthSendEmailResponseDTO> sendEmailVerificationCode(
            @Valid
            AuthSendEmailRequestDTO request
    );

    @Operation(
            summary = "이메일 인증 코드 확인 API",
            description = "이메일로 발송된 인증 코드를 검증합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<AuthVerifyEmailResponseDTO> confirmEmailVerificationCode(
            @Valid
            AuthVerifyEmailRequestDTO request
    );

    @Operation(
            summary = "이메일 인증 코드 재발송 API",
            description = "기존 인증 코드가 만료되었을 경우, 새 인증 코드를 발송합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 코드 재발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<?> resendEmailVerificationCode(
            @Valid
            AuthResendEmailRequestDTO request
    );

    @Operation(
            summary = "로그인 API",
            description = """
            이메일과 비밀번호로 로그인을 진행합니다.
            로그인에 성공하면 JWT 액세스 토큰을 응답 바디로 반환하고,
            리프레시 토큰은 HttpOnly 쿠키로 발급됩니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<AuthLoginResponseDTO> login(
            @Valid AuthLoginRequestDTO request,
            HttpServletResponse response
    );

    @Operation(
            summary = "액세스 토큰 재발급 API",
            description = "쿠키의 refreshToken이 유효하면 새 accessToken을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<AuthRefreshResponseDTO> refresh(
            HttpServletRequest request
    );
}
