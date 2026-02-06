package org.umc.travlocksserver.domain.member.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.travlocksserver.domain.member.dto.request.MemberPasswordUpdateRequestDTO;
import org.umc.travlocksserver.domain.member.dto.request.MemberProfileUpdateRequestDTO;
import org.umc.travlocksserver.domain.member.dto.request.MemberSignupRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberNicknameExistsResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberProfileResponseDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCardResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCursorResponseDTO;
import org.umc.travlocksserver.domain.member.dto.request.MemberWithdrawRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.*;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.global.response.ErrorResponse;
import org.umc.travlocksserver.global.response.PageResponseDTO;
import org.umc.travlocksserver.global.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Tag(name = "회원 API", description = "회원 관련 API 입니다.")
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
			@ApiResponse(responseCode = "401", description = "인증 실패(예: signupToken 만료/불일치)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "충돌(예: 이메일/닉네임 중복)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
			@RequestParam(name = "cursor", required = false) Long cursor,
			@RequestParam(name = "limit", defaultValue = "9") int limit
	);

	@Operation(
			summary = "내 즐겨찾기 목록 조회 API",
			description = """
					로그인한 유저의 즐겨찾기(찜) 템플릿 목록을 조회합니다.
					
					[Query Params]
					- cursor: 다음 목록 조회를 위한 커서(마지막으로 받은 templateId). 첫 조회는 null
					- limit: 한 번에 가져올 템플릿 개수(기본 9)
					
					[Cursor Pagination]
					- hasNext=true 인 경우, 응답의 nextCursor 값을 다음 요청의 cursor로 전달하세요.
					"""
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "내 즐겨찾기 목록 조회 성공"),
			@ApiResponse(responseCode = "400", description = "요청 값 검증 실패 (cursor/limit 범위 오류 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 유저", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<SuccessResponse<TemplateCursorResponseDTO>> getMyFavoriteTemplates(
			@Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
			@RequestParam(name = "cursor", required = false) Long cursor,
			@RequestParam(name = "limit", defaultValue = "9") int limit
	);

	@Operation(
			summary = "마이페이지 조회 API",
			description = """
					로그인한 사용자의 마이페이지 정보를 조회합니다.
					
					- 닉네임 / 한줄 소개
					- 선호 여행 스타일 ID 목록
					- 선호 여행 테마 ID 목록
					- 내가 생성한 블록 / 템플릿 / 즐겨찾기 수
					- 최근 생성한 블록 최대 4개
					- 최근 생성한(=최근 사용) 템플릿 최대 4개
					"""
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "마이페이지 조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패(토큰 없음/만료/유효하지 않음)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 유저", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<SuccessResponse<MemberMyPageResponseDTO>> getMyPage(
			@Parameter(hidden = true) Member member
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

	@Operation(
			summary = "회원 탈퇴 API",
			description = """
					로그인한 사용자가 계정을 탈퇴합니다.
					
					- 탈퇴 사유(reason)는 선택값입니다. (요청 Body 생략 가능)
					- 탈퇴 시 처리 정책:
					  - 개인 데이터(선호스타일/선호테마/약관동의/즐겨찾기/OAuth 계정)는 삭제됩니다.
					  - 사용자가 생성한 콘텐츠(블록/템플릿/템플릿 평점)는 삭제하지 않고 유지되며, 작성자는 '탈퇴한 사용자(더미 계정)'로 변경됩니다.
					  - refreshToken은 서버(Redis)에서 무효화되고, 클라이언트 쿠키도 만료 처리됩니다.
					  - members는 소프트 삭제(status=DELETED) + 개인정보 익명화 처리됩니다.
					"""
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패(토큰 없음/만료/유효하지 않음)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 유저", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<SuccessResponse<Void>> withdraw(
			Member member,
			MemberWithdrawRequestDTO request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	);

	@Operation(
			summary = "마이페이지 내 템플릿 조회 API",
			description = """
							마이페이지에서 내 템플릿 리스트를 조회하는 API입니다.
							페이지네이션이 있기 때문에 query param으로 page를 받습니다.
							이때 첫페이지일 경우 page = 0입니다.
					"""
	)
	@ApiResponse(responseCode = "200", description = "템플릿 리스트 조회에 성공했습니다.")
	ResponseEntity<SuccessResponse<PageResponseDTO<TemplateCardResponseDTO>>> getMyTemplates(
			Long memberId,
			Pageable pageable
	);
}
