package org.umc.travlocksserver.domain.auth.service.command;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.auth.dto.request.AuthOAuthGoogleLoginRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthOAuthLoginResponseDTO;
import org.umc.travlocksserver.domain.auth.enums.OAuthProvider;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.oauth.google.GoogleIdTokenVerifier;
import org.umc.travlocksserver.domain.auth.service.support.OAuthMemberFactory;
import org.umc.travlocksserver.domain.member.entity.Member;

@Service
@RequiredArgsConstructor
public class OAuthGoogleLoginService {

	private final GoogleIdTokenVerifier googleIdTokenVerifier;
	private final OAuthMemberFactory oAuthMemberFactory;
	private final AuthService authService;

	@Transactional
	public AuthOAuthLoginResponseDTO login(
		AuthOAuthGoogleLoginRequestDTO request,
		HttpServletResponse response) {
		// idToken 검증
		Jwt jwt;
		try {
			jwt = googleIdTokenVerifier.verify(request.idToken());
		} catch (Exception e) {
			throw new AuthException(AuthErrorCode.OAUTH_INVALID_TOKEN);
		}

		// claim 추출
		String providerUserId = jwt.getSubject(); // sub
		String email = jwt.getClaimAsString("email");
		Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

		if (providerUserId == null || providerUserId.isBlank()) {
			throw new AuthException(AuthErrorCode.OAUTH_INVALID_TOKEN);
		}
		if (email == null || email.isBlank()) {
			throw new AuthException(AuthErrorCode.OAUTH_INVALID_TOKEN);
		}

		// 로그인 or 신규 생성
		Member member = oAuthMemberFactory.getOrCreateOnboardingMember(
			OAuthProvider.GOOGLE,
			providerUserId,
			email,
			Boolean.TRUE.equals(emailVerified));

		// 토큰 발급
		AuthService.IssuedTokens tokens = authService.issueTokens(member.getId(), response);

		return new AuthOAuthLoginResponseDTO(
			member.getId(),
			member.getStatus(),
			tokens.accessToken(),
			tokens.accessTokenExpiresIn());
	}

}
