package org.umc.travlocksserver.domain.member.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.member.dto.request.MemberSignupRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberNicknameExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberProfileResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
import org.umc.travlocksserver.domain.member.exception.code.MemberSuccessCode;
import org.umc.travlocksserver.domain.member.service.command.MemberSignupService;
import org.umc.travlocksserver.domain.member.service.query.MemberEmailCheckService;
import org.umc.travlocksserver.domain.member.service.query.MemberNicknameCheckService;
import org.umc.travlocksserver.domain.member.service.query.MemberProfileQueryService;
import org.umc.travlocksserver.global.response.SuccessResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberControllerDocs {

	private final MemberEmailCheckService memberEmailCheckService;
	private final MemberNicknameCheckService memberNicknameCheckService;
	private final MemberSignupService memberSignupService;
	private final MemberProfileQueryService memberProfileQueryService;

	@GetMapping("/email/exists")
	public SuccessResponse<MemberEmailExistsResponseDTO> checkEmailExists(
		@RequestParam
		@NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바르지 않은 이메일 형식입니다.")
		String email) {
		MemberEmailExistsResponseDTO data = memberEmailCheckService.checkEmailExists(email);

		return SuccessResponse.ok(MemberSuccessCode.EMAIL_EXISTS_CHECK_SUCCESS, data);
	}

	@GetMapping("/nickname/exists")
	public SuccessResponse<MemberNicknameExistsResponseDTO> checkNicknameExists(
		@RequestParam
		@NotBlank(message = "닉네임은 필수입니다.")
		String nickname) {
		MemberNicknameExistsResponseDTO data = memberNicknameCheckService.checkNicknameExists(nickname);

		return SuccessResponse.ok(MemberSuccessCode.NICKNAME_EXISTS_CHECK_SUCCESS, data);
	}

	@PostMapping("/signup")
	public SuccessResponse<MemberSignupResponseDTO> signup(
		@Valid
		@RequestBody MemberSignupRequestDTO request
	) {
		MemberSignupResponseDTO data = memberSignupService.signup(request);

		return SuccessResponse.ok(MemberSuccessCode.MEMBER_SIGNUP_SUCCESS, data);
	}

	@GetMapping("/{memberId}/profile")
	public SuccessResponse<MemberProfileResponseDTO> getMemberProfile(
		@PathVariable Long memberId,
		@RequestParam(name = "cursor", required = false) Long cursor,
		@RequestParam(name = "limit", defaultValue = "9") int limit
	) {
		MemberProfileResponseDTO data = memberProfileQueryService.getMemberProfile(memberId, cursor, limit);

		return SuccessResponse.ok(MemberSuccessCode.MEMBER_PROFILE_GET_SUCCESS, data);
	}
}