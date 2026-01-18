package org.umc.travlocksserver.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.travlocksserver.domain.auth.dto.request.AuthResendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthSendEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.request.AuthVerifyEmailRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthSendEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthVerifyEmailResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.code.AuthSuccessCode;
import org.umc.travlocksserver.domain.member.service.MemberEmailCheckService;
import org.umc.travlocksserver.domain.auth.service.EmailVerificationService;
import org.umc.travlocksserver.global.response.SuccessResponse;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final EmailVerificationService emailVerificationService;
	private final MemberEmailCheckService authEmailCheckService;

	@PostMapping("/email-verification")
	public SuccessResponse<AuthSendEmailResponseDTO> sendEmailVerificationCode(
		@Valid @RequestBody
		AuthSendEmailRequestDTO request) {
		AuthSendEmailResponseDTO data = emailVerificationService.sendVerificationCode(request.email());

		return SuccessResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CODE_SENT, data);
	}

	@PostMapping("/email-verification/confirm")
	public SuccessResponse<AuthVerifyEmailResponseDTO> confirmEmailVerificationCode(
		@Valid @RequestBody
		AuthVerifyEmailRequestDTO request) {
		AuthVerifyEmailResponseDTO data = emailVerificationService.confirmVerificationCode(
			request.verificationId(),
			request.code());

		return SuccessResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CONFIRMED, data);
	}

	@PostMapping("/email-verification/resend")
	public SuccessResponse<?> resendEmailVerificationCode(
		@Valid @RequestBody
		AuthResendEmailRequestDTO request) {
		emailVerificationService.resendVerificationCode(request.verificationId());
		return SuccessResponse.ok(AuthSuccessCode.EMAIL_VERIFICATION_CODE_RESENT);
	}
}
