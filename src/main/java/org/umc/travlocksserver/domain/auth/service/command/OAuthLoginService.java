package org.umc.travlocksserver.domain.auth.service.command;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.auth.dto.request.AuthOAuthLoginRequestDTO;
import org.umc.travlocksserver.domain.auth.dto.response.AuthOAuthLoginResponseDTO;
import org.umc.travlocksserver.domain.auth.entity.OAuthAccount;
import org.umc.travlocksserver.domain.auth.enums.OAuthProvider;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.exception.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.oauth.google.GoogleIdTokenVerifier;
import org.umc.travlocksserver.domain.auth.repository.OAuthAccountRepository;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.enums.MemberStatus;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.global.profile.DefaultProfileImageProvider;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    private final OAuthAccountRepository oauthAccountRepository;
    private final MemberRepository memberRepository;

    private final AuthService authService;
    private final DefaultProfileImageProvider defaultProfileImageProvider;

    @Transactional
    public AuthOAuthLoginResponseDTO oauthLogin(
            String providerParam,
            AuthOAuthLoginRequestDTO request,
            HttpServletResponse response) {
        OAuthProvider provider = parseProvider(providerParam);

        if (provider != OAuthProvider.GOOGLE) {
            throw new AuthException(AuthErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED);
        }

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
        Member member = oauthAccountRepository
                .findByProviderAndProviderId(provider, providerUserId)
                .map(OAuthAccount::getMember)
                .orElseGet(() -> createOnboardingMember(provider, providerUserId, email, Boolean.TRUE.equals(emailVerified)));

        // 토큰 발급
        AuthService.IssuedTokens tokens = authService.issueTokens(member.getId(), response);

        return new AuthOAuthLoginResponseDTO(
                member.getId(),
                member.getStatus(),
                tokens.accessToken(),
                tokens.accessTokenExpiresIn()
        );
    }


    /**
     * 신규 회원 생성
     */
    private Member createOnboardingMember(OAuthProvider provider, String providerUserId, String email, boolean emailVerified) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String profileImageUrl = defaultProfileImageProvider.pickRandomUrl();
        String tempNickname = "user-" + UUID.randomUUID().toString().substring(0, 8);

        Member member = Member.builder()
                .email(email)
                .nickname(tempNickname)
                .passwordHash(null)
                .status(MemberStatus.ONBOARDING)
                .emailVerified(emailVerified)
                .profileImageUrl(profileImageUrl)
                .vlockCount(0)
                .templateCount(0)
                .starCount(0)
                .build();

        Member saved = memberRepository.save(member);

        oauthAccountRepository.save(
                OAuthAccount.builder()
                        .member(saved)
                        .provider(provider)
                        .providerId(providerUserId)
                        .build()
        );

        return saved;
    }

    /**
     * provider 파싱
     */
    private OAuthProvider parseProvider(String providerParam) {
        try {
            return OAuthProvider.valueOf(providerParam.toUpperCase());
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED);
        }
    }
}

