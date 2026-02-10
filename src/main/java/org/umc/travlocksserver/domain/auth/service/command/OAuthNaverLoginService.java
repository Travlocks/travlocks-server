package org.umc.travlocksserver.domain.auth.service.command;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.auth.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.dto.request.AuthOAuthNaverLoginRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthOAuthLoginResponseDTO;
import org.umc.travlocksserver.domain.auth.enums.OAuthProvider;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.oauth.naver.NaverOAuthClient;
import org.umc.travlocksserver.domain.auth.oauth.naver.NaverProfileResponse;
import org.umc.travlocksserver.domain.auth.oauth.naver.NaverTokenResponse;
import org.umc.travlocksserver.domain.auth.service.support.OAuthMemberFactory;
import org.umc.travlocksserver.domain.member.entity.Member;

@Service
@RequiredArgsConstructor
public class OAuthNaverLoginService {

    private final NaverOAuthClient naverOAuthClient;
    private final OAuthMemberFactory oAuthMemberFactory;
    private final AuthService authService;

    @Transactional
    public AuthOAuthLoginResponseDTO login(
            AuthOAuthNaverLoginRequestDTO request,
            HttpServletResponse response
    ) {
        // code -> token
        NaverTokenResponse token = naverOAuthClient.exchangeCodeForToken(request.code(), request.state());
        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            throw new AuthException(AuthErrorCode.OAUTH_INVALID_TOKEN);
        }

        // token -> profile
        NaverProfileResponse profile = naverOAuthClient.fetchUserProfile(token.accessToken());
        if (profile == null || profile.response() == null || profile.response().id() == null || profile.response().id().isBlank()) {
            throw new AuthException(AuthErrorCode.OAUTH_INVALID_TOKEN);
        }

        String providerUserId = profile.response().id();
        String email = profile.response().email();

        boolean emailVerified = (email != null && !email.isBlank());

        // 로그인 or 신규 생성
        Member member = oAuthMemberFactory.getOrCreateOnboardingMember(
                OAuthProvider.NAVER,
                providerUserId,
                email,
                emailVerified
        );

        // 토큰 발급
        AuthService.IssuedTokens issued = authService.issueTokens(member.getId(), response);

        return new AuthOAuthLoginResponseDTO(
                member.getId(),
                member.getStatus(),
                issued.accessToken(),
                issued.accessTokenExpiresIn()
        );
    }

}
