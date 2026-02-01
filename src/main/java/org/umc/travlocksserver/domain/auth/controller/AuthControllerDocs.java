package org.umc.travlocksserver.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.umc.travlocksserver.domain.auth.dto.request.AuthLoginRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthResendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthSendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthVerifyEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthLoginResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthRefreshResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthVerifyEmailResponseDTO;
import org.umc.travlocksserver.global.response.ErrorResponse;
import org.umc.travlocksserver.global.response.SuccessResponse;

public interface AuthControllerDocs {

    @Operation(
            summary = "이메일 인증 코드 발송 API",
            description ="""
            회원가입을 위한 이메일 인증 코드를 발송합니다.

            - 입력한 이메일로 인증 코드가 전송됩니다.
            - 응답으로 verificationId가 반환되며, verificationId는 이후 인증 확인/재발송 시 사용됩니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 코드 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / 이미 가입된 이메일 / 인증 요청 정보 오류",content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "이메일 발송 실패",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthSendEmailResponseDTO>> sendEmailVerificationCode(
            @Valid AuthSendEmailRequestDTO request
    );

    @Operation(
            summary = "이메일 인증 코드 확인 API",
            description = """
            이메일로 발송된 인증 코드를 검증합니다.
            
            - verificationId가 유효하고, code가 일치하면 인증에 성공합니다.
            - 인증에 성공하면 verificationId에 매핑된 이메일 기준으로
              회원가입에 사용 가능한 signupToken을 발급합니다.
            - 인증 성공 시 재사용 방지를 위해 verificationId는 삭제됩니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / verificationId 만료·없음 / 인증 코드 불일치",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthVerifyEmailResponseDTO>> confirmEmailVerificationCode(
            @Valid AuthVerifyEmailRequestDTO request
    );

    @Operation(
            summary = "이메일 인증 코드 재발송 API",
            description = """
            기존 인증 코드가 만료되었거나 재발송이 필요한 경우 새 인증 코드를 발송합니다.

            - verificationId를 기준으로 기존 인증 요청을 찾아 새 코드를 발송합니다.
            - 발송 성공 시, 동일한 verificationId로 인증 코드가 갱신됩니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 코드 재발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / verificationId 만료·없음",content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "이메일 발송 실패",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<?>> resendEmailVerificationCode(
            @Valid AuthResendEmailRequestDTO request
    );

    @Operation(
            summary = "로그인 API",
            description = """
            이메일과 비밀번호로 로그인을 진행합니다.
            
            - 성공 시 accessToken은 응답 바디로 반환됩니다.
            - refreshToken은 HttpOnly 쿠키(Set-Cookie)로 발급됩니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패",content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 실패(이메일 또는 비밀번호 불일치)",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthLoginResponseDTO>> login(
            @Valid AuthLoginRequestDTO request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "액세스 토큰 재발급 API",
            description = """
            쿠키의 refreshToken이 유효하면 새 accessToken을 발급합니다.
            
            - Access Token이 만료된 경우(ACCESS_TOKEN_EXPIRED),
              본 API를 호출하여 accessToken을 재발급 받을 수 있습니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "refreshToken 누락 / 유효하지 않음 / 만료됨",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthRefreshResponseDTO>> refresh(
            @Parameter(hidden = true) HttpServletRequest request
    );
}
