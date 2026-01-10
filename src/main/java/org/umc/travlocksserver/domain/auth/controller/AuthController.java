package org.umc.travlocksserver.domain.auth.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.umc.travlocksserver.domain.auth.dto.*;
import org.umc.travlocksserver.domain.auth.exception.code.AuthSuccessCode;
import org.umc.travlocksserver.domain.auth.service.AuthEmailCheckService;
import org.umc.travlocksserver.domain.auth.service.EmailVerificationService;
import org.umc.travlocksserver.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final AuthEmailCheckService authEmailCheckService;

    @PostMapping("/email-verification")
    public ApiResponse<AuthSendEmailResponseDTO> sendEmailVerificationCode(
            @Valid @RequestBody AuthSendEmailRequestDTO request
    ) {
        AuthSendEmailResponseDTO data =
                emailVerificationService.sendVerificationCode(request.email());

        return ApiResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CODE_SENT, data);
    }

    @GetMapping("/email/exists")
    public ApiResponse<AuthEmailExistsResponseDTO> checkEmailExists(
            @RequestParam
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email
    ) {
        AuthEmailExistsResponseDTO data = authEmailCheckService.checkEmailExists(email);

        AuthSuccessCode successCode = data.exists()
                ? AuthSuccessCode.EMAIL_ALREADY_EXISTS
                : AuthSuccessCode.EMAIL_AVAILABLE;

        return ApiResponse.ok(successCode, data);
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
