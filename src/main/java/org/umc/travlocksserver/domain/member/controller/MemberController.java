package org.umc.travlocksserver.domain.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.umc.travlocksserver.domain.member.dto.request.MemberPasswordUpdateRequestDTO;
import org.umc.travlocksserver.domain.member.dto.request.MemberSignupRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberNicknameExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberProfileResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberSuccessCode;
import org.umc.travlocksserver.domain.member.service.command.MemberPasswordUpdateService;
import org.umc.travlocksserver.domain.member.service.command.MemberSignupService;
import org.umc.travlocksserver.domain.member.service.query.MemberEmailCheckService;
import org.umc.travlocksserver.domain.member.service.query.MemberNicknameCheckService;
import org.umc.travlocksserver.domain.member.service.query.MemberProfileQueryService;
import org.umc.travlocksserver.global.annotation.LoginUser;
import org.umc.travlocksserver.global.response.SuccessResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.umc.travlocksserver.global.security.code.SecurityErrorCode;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberControllerDocs {

    private final MemberEmailCheckService memberEmailCheckService;
    private final MemberNicknameCheckService memberNicknameCheckService;
    private final MemberSignupService memberSignupService;
    private final MemberProfileQueryService memberProfileQueryService;
    private final MemberPasswordUpdateService memberPasswordUpdateService;

    @GetMapping("/email/exists")
    public ResponseEntity<SuccessResponse<MemberEmailExistsResponseDTO>> checkEmailExists(
            @RequestParam
            @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email) {
        MemberSuccessCode successCode = MemberSuccessCode.EMAIL_EXISTS_CHECK_SUCCESS;
        MemberEmailExistsResponseDTO data = memberEmailCheckService.checkEmailExists(email);

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, data));
    }

    @GetMapping("/nickname/exists")
    public ResponseEntity<SuccessResponse<MemberNicknameExistsResponseDTO>> checkNicknameExists(
            @RequestParam
            @NotBlank(message = "닉네임은 필수입니다.")
            String nickname) {
        MemberSuccessCode successCode = MemberSuccessCode.NICKNAME_EXISTS_CHECK_SUCCESS;
        MemberNicknameExistsResponseDTO data = memberNicknameCheckService.checkNicknameExists(nickname);

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, data));
    }

    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<MemberSignupResponseDTO>> signup(
            @Valid @RequestBody MemberSignupRequestDTO request,
            HttpServletResponse response) {
        MemberSuccessCode successCode = MemberSuccessCode.MEMBER_SIGNUP_SUCCESS;
        MemberSignupResponseDTO data = memberSignupService.signup(request, response);

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, data));
    }

    @GetMapping("/{memberId}/profile")
    public ResponseEntity<SuccessResponse<MemberProfileResponseDTO>> getMemberProfile(
            @PathVariable Long memberId,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "limit", defaultValue = "9") int limit
    ) {
        MemberSuccessCode successCode = MemberSuccessCode.MEMBER_PROFILE_GET_SUCCESS;

        MemberProfileResponseDTO data = memberProfileQueryService.getMemberProfile(memberId, cursor, limit);

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, data));
    }

    @PatchMapping("/password")
    public ResponseEntity<SuccessResponse<Void>> updatePassword(
            @LoginUser Member member,
            @Valid @RequestBody MemberPasswordUpdateRequestDTO request) {
        MemberSuccessCode successCode = MemberSuccessCode.MEMBER_PASSWORD_UPDATED;

        memberPasswordUpdateService.updatePassword(
                member.getId(),
                request.currentPassword(),
                request.newPassword()
        );

        return ResponseEntity
                .status(successCode.getStatus())
                .body(SuccessResponse.ok(successCode, null));
    }
}