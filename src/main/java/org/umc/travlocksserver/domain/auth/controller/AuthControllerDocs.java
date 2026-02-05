package org.umc.travlocksserver.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.travlocksserver.domain.auth.dto.request.*;
import org.umc.travlocksserver.domain.auth.dto.response.*;
import org.umc.travlocksserver.global.response.ErrorResponse;
import org.umc.travlocksserver.global.response.SuccessResponse;

@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthControllerDocs {

    @Operation(
            summary = "이메일 인증 코드 발송 API",
            description ="""
            회원가입을 위한 이메일 인증 코드를 발송합니다.

            - 입력한 이메일로 인증 코드가 전송됩니다.
            - 응답으로 verificationId가 반환되며, verificationId는 이후 인증 확인/재발송 시 사용됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 코드 발송 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / 이미 가입된 이메일 / 인증 요청 정보 오류",content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "이메일 발송 실패",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthSendEmailResponseDTO>> sendEmailVerificationCode(
            @Valid AuthSendEmailRequestDTO request
    );

    @Operation(
            summary = "이메일 인증 코드 확인 API",
            description = """
            이메일로 발송된 인증 코드를 검증합니다.
            
            - verificationId가 유효하고, code가 일치하면 인증에 성공합니다.
            - 인증에 성공하면 verificationId에 매핑된 이메일 기준으로
              회원가입에 사용 가능한 signupToken을 발급합니다.
            - 인증 성공 시 재사용 방지를 위해 verificationId는 삭제됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / verificationId 만료·없음 / 인증 코드 불일치",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthVerifyEmailResponseDTO>> confirmEmailVerificationCode(
            @Valid AuthVerifyEmailRequestDTO request
    );

    @Operation(
            summary = "이메일 인증 코드 재발송 API",
            description = """
            기존 인증 코드가 만료되었거나 재발송이 필요한 경우 새 인증 코드를 발송합니다.

            - verificationId를 기준으로 기존 인증 요청을 찾아 새 코드를 발송합니다.
            - 발송 성공 시, 동일한 verificationId로 인증 코드가 갱신됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 코드 재발송 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / verificationId 만료·없음",content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "이메일 발송 실패",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<?>> resendEmailVerificationCode(
            @Valid AuthResendEmailRequestDTO request
    );

    @Operation(
            summary = "로그인 API",
            description = """
            이메일과 비밀번호로 로그인을 진행합니다.
            
            - 성공 시 accessToken은 응답 바디로 반환됩니다.
            - refreshToken은 HttpOnly 쿠키(Set-Cookie)로 발급됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 실패(이메일 또는 비밀번호 불일치)",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthLoginResponseDTO>> login(
            @Valid AuthLoginRequestDTO request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "액세스 토큰 재발급 API",
            description = """
            쿠키의 refreshToken이 유효하면 새 accessToken을 발급합니다.
            
            - Access Token이 만료된 경우(ACCESS_TOKEN_EXPIRED),
              본 API를 호출하여 accessToken을 재발급 받을 수 있습니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공"),
            @ApiResponse(responseCode = "400", description = "refreshToken 누락 / 유효하지 않음 / 만료됨",content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthRefreshResponseDTO>> refresh(
            @Parameter(hidden = true) HttpServletRequest request
    );

    @Operation(
            summary = "로그아웃 API",
            description = """
            refreshToken을 무효화하고(refreshToken:{jti} Redis 삭제), 쿠키의 refreshToken을 삭제합니다.

            - refreshToken은 HttpOnly 쿠키로 전달됩니다.
            - 로그아웃은 멱등하게 동작합니다.
              (refreshToken이 없거나 유효하지 않아도 쿠키 삭제 응답은 내려갑니다.)
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    ResponseEntity<SuccessResponse<?>> logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "비밀번호 재설정 링크 전송 API",
            description = """
            로그인하지 못한 사용자가 비밀번호를 재설정할 수 있도록, 입력한 이메일로 비밀번호 재설정 링크를 전송합니다.

            - 입력한 이메일이 서비스에 가입된 이메일인 경우에만 resetToken을 발급하고 메일을 전송합니다.
            - 보안상 계정 존재 여부 노출을 방지하기 위해,
              입력한 이메일이 서비스에 가입되지 않은 경우에도 동일한 성공 응답을 반환합니다.
            - 이 경우 실제 비밀번호 재설정 이메일은 발송되지 않습니다.
            - 메일에는 resetToken이 포함된 재설정 링크가 전달됩니다.
              (예: https://travlocks.kro.kr/reset-password?token=...)
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 링크 전송 요청 처리 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패(이메일 형식 오류 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "이메일 발송 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<?>> requestPasswordResetLink(
            @Valid AuthPasswordResetLinkRequestDTO request
    );

    @Operation(
            summary = "비밀번호 재설정 토큰 유효성 검증 API",
            description = """
            비밀번호 재설정 링크에 포함된 resetToken의 유효성을 검증합니다.

            - resetToken이 Redis에 존재하고 만료되지 않은 경우 valid=true를 반환합니다.
            - resetToken이 없거나 만료된 경우 에러를 반환합니다.
            - 본 API는 비밀번호 재설정 페이지 진입 시 프론트에서 호출하여,
              유효한 토큰일 때만 비밀번호 입력 폼을 노출하는 용도로 사용합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 토큰 검증 성공"),
            @ApiResponse(responseCode = "400", description = "resetToken이 올바르지 않거나 만료됨", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<AuthPasswordResetVerifyResponseDTO>> verifyPasswordResetToken(
            @Parameter(description = "비밀번호 재설정 토큰", required = true)
            @RequestParam("token") String token
    );

    @Operation(
            summary = "비밀번호 재설정 확정 API",
            description = """
        비밀번호 재설정 링크를 통해 전달받은 resetToken을 사용하여
        새로운 비밀번호로 변경을 확정하는 API입니다.

        - resetToken이 유효한 경우에만 비밀번호 변경이 가능합니다.
        - 새 비밀번호와 비밀번호 확인 값이 일치해야 합니다.
        - 비밀번호는 최소 8자 이상이며 영문과 숫자를 포함해야 합니다.
        - 비밀번호 변경이 완료되면 resetToken은 재사용 방지를 위해 삭제됩니다.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
            @ApiResponse(responseCode = "400", description = "resetToken이 올바르지 않거나 만료됨/비밀번호와 비밀번호 확인 불일치", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SuccessResponse<?>> confirmPasswordReset(
            @Valid AuthPasswordResetConfirmRequestDTO request
    );


}
