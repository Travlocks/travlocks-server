package org.umc.travlocksserver.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.auth.dto.AuthSendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.code.AuthSuccessCode;
import org.umc.travlocksserver.domain.auth.service.EmailVerificationService;
import org.umc.travlocksserver.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/email/verification-codes")
    public ApiResponse<AuthSendEmailResponseDTO> sendEmailVerificationCode(
            @Valid @RequestBody AuthSendEmailRequestDTO request
    ) {
        AuthSendEmailResponseDTO data =
                emailVerificationService.sendVerificationCode(request.email());

        return ApiResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CODE_SENT, data);
    }
}
