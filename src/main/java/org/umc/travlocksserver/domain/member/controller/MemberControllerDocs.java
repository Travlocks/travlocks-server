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
            summary = "이메일 중복 검사 API",
            description = """
            회원가입 시 이메일 중복 여부를 확인합니다.
            
            - data.exists=true  : 이미 사용 중인 이메일
            - data.exists=false : 사용 가능한 이메일
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 중복 검사 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패(이메일 형식/빈 값 등)")
    })
    SuccessResponse<MemberEmailExistsResponseDTO> checkEmailExists(
            @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email
    );

    @Operation(
            summary = "닉네임 중복 검사 API",
            description = """
            회원가입 시 닉네임 중복 여부를 확인합니다.
            
            - data.exists=true  : 이미 사용 중인 닉네임
            - data.exists=false : 사용 가능한 닉네임
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 중복 검사 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패(빈 값 등)")
    })
    SuccessResponse<MemberNicknameExistsResponseDTO> checkNicknameExists(
            @NotBlank(message = "닉네임은 필수입니다.")
            String nickname
    );

    @Operation(
            summary = "회원가입 API",
            description = """
            회원가입을 진행합니다.
            
            - 이메일 인증 성공 후 발급된 signupToken이 필요합니다.
            - signupToken에 매핑된 email과 요청 email이 일치해야 합니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / 약관 오류 / 존재하지 않는 여행 스타일·테마 ID 포함"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(예: signupToken 만료/불일치)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "충돌(예: 이메일/닉네임 중복)")
    })
    SuccessResponse<MemberSignupResponseDTO> signup(
            @Valid MemberSignupRequestDTO request
    );
}
