package org.umc.travlocksserver.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    SuccessResponse<MemberEmailExistsResponseDTO> checkEmailExists(String email);

    @Operation(
            summary = "닉네임 중복 체크 API",
            description = "회원가입 시 닉네임 중복 여부를 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 중복 체크 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<MemberNicknameExistsResponseDTO> checkNicknameExists(String nickname);

    @Operation(
            summary = "회원가입 API",
            description = "회원가입을 진행합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    SuccessResponse<MemberSignupResponseDTO> signup(MemberSignupRequestDTO request);
}
