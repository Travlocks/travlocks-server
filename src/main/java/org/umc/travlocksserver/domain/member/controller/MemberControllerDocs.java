package org.umc.travlocksserver.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.umc.travlocksserver.domain.member.dto.request.MemberSignupRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberNicknameExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
import org.umc.travlocksserver.global.response.SuccessResponse;

public interface MemberControllerDocs {

    @Operation(
            summary = "이메일 중복 체크 API",
            description = "회원가입 시 이메일 중복 여부를 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 중복 체크 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<MemberEmailExistsResponseDTO> checkEmailExists(
            @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email
    );

    @Operation(
            summary = "닉네임 중복 체크 API",
            description = "회원가입 시 닉네임 중복 여부를 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 중복 체크 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<MemberNicknameExistsResponseDTO> checkNicknameExists(
            @NotBlank(message = "닉네임은 필수입니다.")
            String nickname
    );

    @Operation(
            summary = "회원가입 API",
            description = "회원가입을 진행합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<MemberSignupResponseDTO> signup(
            @Valid
            MemberSignupRequestDTO request
    );
}
