package org.umc.travlocksserver.domain.member.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.member.dto.response.MemberEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberNicknameExistsResponseDTO;
import org.umc.travlocksserver.domain.member.exception.code.MemberSuccessCode;
import org.umc.travlocksserver.domain.member.service.MemberEmailCheckService;
import org.umc.travlocksserver.domain.member.service.MemberNicknameCheckService;
import org.umc.travlocksserver.global.response.ApiResponse;
import org.umc.travlocksserver.global.response.SuccessResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberEmailCheckService memberEmailCheckService;
    private final MemberNicknameCheckService memberNicknameCheckService;

    @GetMapping("/email/exists")
    public SuccessResponse<MemberEmailExistsResponseDTO> checkEmailExists(
            @RequestParam
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email
    ) {
        MemberEmailExistsResponseDTO data = memberEmailCheckService.checkEmailExists(email);

        MemberSuccessCode successCode = data.exists()
                ? MemberSuccessCode.EMAIL_ALREADY_EXISTS
                : MemberSuccessCode.EMAIL_AVAILABLE;

        return SuccessResponse.ok(successCode, data);
    }

    @GetMapping("/nickname/exists")
    public SuccessResponse<MemberNicknameExistsResponseDTO> checkNicknameExists(
            @RequestParam
            @NotBlank(message = "닉네임은 필수입니다.")
            String nickname
    ) {
        MemberNicknameExistsResponseDTO data =
                memberNicknameCheckService.checkNicknameExists(nickname);

        MemberSuccessCode successCode = data.available()
                ? MemberSuccessCode.NICKNAME_AVAILABLE
                : MemberSuccessCode.NICKNAME_ALREADY_EXISTS;

        return SuccessResponse.ok(successCode, data);
    }
}