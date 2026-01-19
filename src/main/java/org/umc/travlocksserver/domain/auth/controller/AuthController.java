package org.umc.travlocksserver.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.travlocksserver.domain.auth.dto.request.AuthLoginRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthResendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthSendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthVerifyEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthLoginResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthRefreshResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthVerifyEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.code.AuthSuccessCode;
import org.umc.travlocksserver.domain.auth.service.AuthService;
import org.umc.travlocksserver.domain.member.service.MemberEmailCheckService;
import org.umc.travlocksserver.domain.auth.service.EmailVerificationService;
import org.umc.travlocksserver.global.response.SuccessResponse;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {

	private final EmailVerificationService emailVerificationService;
    private final AuthService authService;

	@PostMapping("/email-verification")
	public SuccessResponse<AuthSendEmailResponseDTO> sendEmailVerificationCode(
		@RequestBody
		AuthSendEmailRequestDTO request) {
		AuthSendEmailResponseDTO data = emailVerificationService.sendVerificationCode(request.email());

		return SuccessResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CODE_SENT, data);
	}

	@PostMapping("/email-verification/confirm")
	public SuccessResponse<AuthVerifyEmailResponseDTO> confirmEmailVerificationCode(
		@RequestBody
		AuthVerifyEmailRequestDTO request) {
		AuthVerifyEmailResponseDTO data = emailVerificationService.confirmVerificationCode(
			request.verificationId(),
			request.code());

		return SuccessResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CONFIRMED, data);
	}

	@PostMapping("/email-verification/resend")
	public SuccessResponse<?> resendEmailVerificationCode(
		@RequestBody
		AuthResendEmailRequestDTO request) {
		emailVerificationService.resendVerificationCode(request.verificationId());
		return SuccessResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CODE_RESENT);
	}

    @PostMapping("/login")
    public SuccessResponse<AuthLoginResponseDTO> login(
            @Valid @RequestBody AuthLoginRequestDTO request,
            HttpServletResponse response) {
        AuthLoginResponseDTO data = authService.login(request, response);
        return SuccessResponse.ok(AuthSuccessCode.AUTH_LOGIN_SUCCESS, data);
    }

    @PostMapping("/refresh")
    public SuccessResponse<AuthRefreshResponseDTO> refresh(
            HttpServletRequest request) {
        AuthRefreshResponseDTO data = authService.refreshAccessToken(request);
        return SuccessResponse.ok(AuthSuccessCode.AUTH_ACCESS_TOKEN_REISSUED, data);
    }
}
