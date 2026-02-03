package org.umc.travlocksserver.domain.member.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.travlocksserver.domain.member.dto.request.MemberPasswordUpdateRequestDTO;
import org.umc.travlocksserver.domain.member.dto.request.MemberProfileUpdateRequestDTO;
import org.umc.travlocksserver.domain.member.dto.request.MemberSignupRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.*;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.global.response.ErrorResponse;
import org.umc.travlocksserver.global.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Tag(name = "Member", description = "회원 관련 API")
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
		@ApiResponse(responseCode = "200", description = "이메일 중복 검사 성공"),
		@ApiResponse(responseCode = "400", description = "요청 값 검증 실패(이메일 형식/빈 값 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
    ResponseEntity<SuccessResponse<MemberEmailExistsResponseDTO>> checkEmailExists(
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
		@ApiResponse(responseCode = "200", description = "닉네임 중복 검사 성공"),
		@ApiResponse(responseCode = "400", description = "요청 값 검증 실패(빈 값 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
    ResponseEntity<SuccessResponse<MemberNicknameExistsResponseDTO>> checkNicknameExists(
		@NotBlank(message = "닉네임은 필수입니다.")
		String nickname
	);

	@Operation(
		summary = "회원가입 API",
		description = """
			회원가입을 진행합니다.
			            
			- 이메일 인증 성공 후 발급된 signupToken이 필요합니다.
			- signupToken에 매핑된 email과 요청 email이 일치해야 합니다.
			- 회원가입 성공 시 accessToken과 refreshToken이 발급됩니다.
			"""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "회원가입 성공"),
		@ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / 약관 오류 / 존재하지 않는 여행 스타일·테마 ID 포함", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "401", description = "인증 실패(예: signupToken 만료/불일치)",content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "409", description = "충돌(예: 이메일/닉네임 중복)",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
    ResponseEntity<SuccessResponse<MemberSignupResponseDTO>> signup(
		@Valid MemberSignupRequestDTO request,
        HttpServletResponse response
	);

	@Operation(
		summary = "유저 프로필 조회 API",
		description = """
			특정 유저의 프로필과 공개 템플릿 목록을 조회합니다.
			    
			[Query Params]
			- cursor: 다음 목록 조회를 위한 커서(마지막으로 받은 templateId). 첫 조회는 null
			- limit: 한 번에 가져올 템플릿 개수(기본 9)
			    
			[Cursor Pagination]
			- hasNext=true 인 경우, 응답의 nextCursor 값을 다음 요청의 cursor로 전달하세요.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "유저 프로필 조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패 (cursor/limit 범위 오류 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 유저", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<SuccessResponse<MemberProfileResponseDTO>> getMemberProfile(
		@PathVariable Long memberId,
		@RequestParam(name = "cursor", defaultValue = "0") Long cursor,
		@RequestParam(name = "limit", defaultValue = "9") int limit
	);

    @Operation(
            summary = "프로필 편집 API",
            description = """
        마이페이지 프로필을 편집합니다.

        - 요청에 포함되지 않은 필드는 기존 값을 유지합니다.
        - introduction: null 전달 시 소개가 삭제됩니다.
        - preferredTravelStyleIds / preferredTravelThemeIds:
          - 필드 없음 -> 유지
          - null -> 400 오류
          - [] -> 전체 해제
          - [id...] -> 해당 목록으로 교체 (최대 2개)
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패/중복 닉네임/스타일·테마 개수 초과/존재하지 않는 ID", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<MemberProfileUpdateResponseDTO>> updateMyProfile(
            Member member,
            @Valid MemberProfileUpdateRequestDTO request
    );

    @Operation(
            summary = "비밀번호 변경 API",
            description = """
			로그인한 사용자의 비밀번호를 변경합니다.
			            
			- currentPassword가 현재 비밀번호와 일치해야 합니다.
			- newPassword는 기존 비밀번호와 달라야 합니다.
			"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / 현재 비밀번호 불일치 / 새 비밀번호가 기존과 동일", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패(토큰 없음/만료 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<Void>> updatePassword(
            Member member,
            @Valid MemberPasswordUpdateRequestDTO request
    );
}
