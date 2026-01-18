package org.umc.travlocksserver.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.umc.travlocksserver.domain.auth.dto.request.AuthResendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthSendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthVerifyEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthVerifyEmailResponseDTO;
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
}
