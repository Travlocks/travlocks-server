package org.umc.travlocksserver.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.umc.travlocksserver.domain.auth.dto.request.*;
import org.umc.travlocksserver.domain.auth.dto.response.AuthLoginResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthRefreshResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthVerifyEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.code.AuthSuccessCode;
import org.umc.travlocksserver.domain.auth.service.command.AuthService;
import org.umc.travlocksserver.domain.auth.service.command.EmailVerificationService;
import org.umc.travlocksserver.domain.auth.service.command.PasswordResetService;
import org.umc.travlocksserver.global.response.SuccessResponse;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {

	private final EmailVerificationService emailVerificationService;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

	@PostMapping("/email-verification")
	public ResponseEntity<SuccessResponse<AuthSendEmailResponseDTO>> sendEmailVerificationCode(
		@Valid @RequestBody
		AuthSendEmailRequestDTO request) {
        AuthSuccessCode successCode = AuthSuccessCode.EMAIL_VERIFICATION_CODE_SENT;
        AuthSendEmailResponseDTO data = emailVerificationService.sendVerificationCode(request.email());

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, data));
	}

	@PostMapping("/email-verification/confirm")
	public ResponseEntity<SuccessResponse<AuthVerifyEmailResponseDTO>> confirmEmailVerificationCode(
		@Valid @RequestBody
		AuthVerifyEmailRequestDTO request) {
        AuthSuccessCode successCode = AuthSuccessCode.EMAIL_VERIFICATION_CONFIRMED;
		AuthVerifyEmailResponseDTO data = emailVerificationService.confirmVerificationCode(
			request.verificationId(),
			request.code()
        );

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, data));
	}

	@PostMapping("/email-verification/resend")
	public ResponseEntity<SuccessResponse<?>> resendEmailVerificationCode(
		@Valid @RequestBody
		AuthResendEmailRequestDTO request) {
        AuthSuccessCode successCode = AuthSuccessCode.EMAIL_VERIFICATION_CODE_RESENT;
		emailVerificationService.resendVerificationCode(request.verificationId());

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, null));
	}

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthLoginResponseDTO>> login(
            @Valid @RequestBody AuthLoginRequestDTO request,
            HttpServletResponse response) {
        AuthSuccessCode successCode = AuthSuccessCode.AUTH_LOGIN_SUCCESS;
        AuthLoginResponseDTO data = authService.login(request, response);

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, data));
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<AuthRefreshResponseDTO>> refresh(
            HttpServletRequest request) {
        AuthSuccessCode successCode = AuthSuccessCode.AUTH_ACCESS_TOKEN_REISSUED;
        AuthRefreshResponseDTO data = authService.refreshAccessToken(request);

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, data));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<?>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthSuccessCode successCode = AuthSuccessCode.AUTH_LOGOUT_SUCCESS;
        authService.logout(request, response);

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, null));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<SuccessResponse<?>> requestPasswordResetLink(
            @Valid @RequestBody AuthPasswordResetLinkRequestDTO request
    ) {
        AuthSuccessCode successCode = AuthSuccessCode.PASSWORD_RESET_LINK_SENT;
        passwordResetService.sendPasswordResetLink(request.email());

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, null));
    }

}
