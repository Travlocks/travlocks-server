package org.umc.travlocksserver.domain.member.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.umc.travlocksserver.domain.member.dto.request.MemberSignupRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberNicknameExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
import org.umc.travlocksserver.domain.member.exception.code.MemberSuccessCode;
import org.umc.travlocksserver.domain.member.service.MemberEmailCheckService;
import org.umc.travlocksserver.domain.member.service.MemberNicknameCheckService;
import org.umc.travlocksserver.domain.member.service.MemberSignupService;
import org.umc.travlocksserver.global.response.SuccessResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberEmailCheckService memberEmailCheckService;
    private final MemberNicknameCheckService memberNicknameCheckService;
    private final MemberSignupService memberSignupService;

    @GetMapping("/email/exists")
    public SuccessResponse<MemberEmailExistsResponseDTO> checkEmailExists(
            @RequestParam @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email) {
        MemberEmailExistsResponseDTO data = memberEmailCheckService.checkEmailExists(email);

        MemberSuccessCode successCode = data.exists()
                ? MemberSuccessCode.EMAIL_ALREADY_EXISTS
                : MemberSuccessCode.EMAIL_AVAILABLE;

        return SuccessResponse.ok(successCode, data);
    }

    @GetMapping("/nickname/exists")
    public SuccessResponse<MemberNicknameExistsResponseDTO> checkNicknameExists(
            @RequestParam @NotBlank(message = "닉네임은 필수입니다.")
            String nickname) {
        MemberNicknameExistsResponseDTO data = memberNicknameCheckService.checkNicknameExists(nickname);

        MemberSuccessCode successCode = data.available()
                ? MemberSuccessCode.NICKNAME_AVAILABLE
                : MemberSuccessCode.NICKNAME_ALREADY_EXISTS;

        return SuccessResponse.ok(successCode, data);
    }

    @PostMapping("/signup")
    public SuccessResponse<MemberSignupResponseDTO> signup(
            @Valid @RequestBody MemberSignupRequestDTO request
    ) {
        MemberSignupResponseDTO data = memberSignupService.signup(request);

        return SuccessResponse.ok(MemberSuccessCode.MEMBER_SIGNUP_SUCCESS, data);
    }
}