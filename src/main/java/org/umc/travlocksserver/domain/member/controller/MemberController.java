package org.umc.travlocksserver.domain.member.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.umc.travlocksserver.domain.member.dto.MemberEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.auth.exception.code.AuthSuccessCode;
import org.umc.travlocksserver.domain.member.exception.code.MemberSuccessCode;
import org.umc.travlocksserver.domain.member.service.MemberEmailCheckService;
import org.umc.travlocksserver.global.apiPayload.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberEmailCheckService memberEmailCheckService;

    @GetMapping("/email/exists")
    public ApiResponse<MemberEmailExistsResponseDTO> checkEmailExists(
            @RequestParam
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email
    ) {
        MemberEmailExistsResponseDTO data = memberEmailCheckService.checkEmailExists(email);

        MemberSuccessCode successCode = data.exists()
                ? MemberSuccessCode.EMAIL_ALREADY_EXISTS
                : MemberSuccessCode.EMAIL_AVAILABLE;

        return ApiResponse.ok(successCode, data);
    }
}