package org.umc.travlocksserver.domain.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.travlocksserver.domain.member.dto.request.MemberSignupRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberNicknameExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberProfileResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
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

@Tag(name = "템플릿 API", description = "템플릿 관련 API입니다.")
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
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패(이메일 형식/빈 값 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 중복 검사 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패(빈 값 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / 약관 오류 / 존재하지 않는 여행 스타일·테마 ID 포함", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(예: signupToken 만료/불일치)",content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "충돌(예: 이메일/닉네임 중복)",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
}
