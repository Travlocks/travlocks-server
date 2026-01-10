package org.umc.travlocksserver.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.travlocksserver.domain.auth.dto.*;
import org.umc.travlocksserver.domain.auth.exception.code.AuthSuccessCode;
import org.umc.travlocksserver.domain.member.service.MemberEmailCheckService;
import org.umc.travlocksserver.domain.auth.service.EmailVerificationService;
import org.umc.travlocksserver.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final MemberEmailCheckService authEmailCheckService;

    @PostMapping("/email-verification")
    public ApiResponse<AuthSendEmailResponseDTO> sendEmailVerificationCode(
            @Valid @RequestBody AuthSendEmailRequestDTO request
    ) {
        AuthSendEmailResponseDTO data =
                emailVerificationService.sendVerificationCode(request.email());

        return ApiResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CODE_SENT, data);
    }

    @PostMapping("/email-verification/confirm")
    public ApiResponse<org.umc.travlocksserver.domain.auth.dto.AuthVerifyEmailResponseDTO> confirmEmailVerificationCode(
            @Valid @RequestBody AuthVerifyEmailRequestDTO request
    ) {
        AuthVerifyEmailResponseDTO data = emailVerificationService.confirmVerificationCode(
                request.verificationId(),
                request.code()
        );

        return ApiResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CONFIRMED, data);
    }


}
