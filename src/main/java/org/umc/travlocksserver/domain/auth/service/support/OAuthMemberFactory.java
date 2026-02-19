package org.umc.travlocksserver.domain.auth.service.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.auth.code.AuthErrorCode;
import org.umc.travlocksserver.domain.auth.entity.OAuthAccount;
import org.umc.travlocksserver.domain.auth.enums.OAuthProvider;
import org.umc.travlocksserver.domain.auth.exception.AuthException;
import org.umc.travlocksserver.domain.auth.repository.OAuthAccountRepository;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.enums.MemberStatus;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.global.profile.DefaultProfileImageProvider;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthMemberFactory {
	private final MemberRepository memberRepository;
	private final OAuthAccountRepository oauthAccountRepository;
	private final DefaultProfileImageProvider defaultProfileImageProvider;

	@Transactional
	public Member getOrCreateOnboardingMember(OAuthProvider provider, String providerUserId, String email,
		boolean emailVerified) {
		return oauthAccountRepository.findByProviderAndProviderId(provider, providerUserId)
			.map(OAuthAccount::getMember)
                .map(member -> {
                    if (member.getStatus() == MemberStatus.DELETED) {
                        throw new AuthException(AuthErrorCode.DELETED_ACCOUNT);
                    }
                    return member;
                })
			.orElseGet(() -> createOnboardingMember(provider, providerUserId, email, emailVerified));
	}

	private Member createOnboardingMember(OAuthProvider provider, String providerUserId, String email,
		boolean emailVerified) {
        if (email != null && !email.isBlank()) {
            memberRepository.findByEmail(email).ifPresent(existing -> {
                if (existing.getStatus() == MemberStatus.DELETED) {
                    throw new AuthException(AuthErrorCode.DELETED_ACCOUNT); // or ACCOUNT_NOT_FOUND
                }
                throw new AuthException(AuthErrorCode.OAUTH_EMAIL_CONFLICT);
            });
        }

		Member member = Member.builder()
			.email(email)
			.nickname(generateUniqueTempNickname())
			.passwordHash(null)
			.status(MemberStatus.ONBOARDING)
			.emailVerified(emailVerified)
			.profileImageUrl(defaultProfileImageProvider.pickRandomUrl())
			.vlockCount(0)
			.templateCount(0)
			.favoriteCount(0)
			.build();

		Member saved = memberRepository.save(member);

		oauthAccountRepository.save(OAuthAccount.builder()
			.member(saved)
			.provider(provider)
			.providerId(providerUserId)
			.build());

		return saved;
	}

	private String generateUniqueTempNickname() {
		for (int i = 0; i < 10; i++) {
			String candidate = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 9);
			if (!memberRepository.existsByNickname(candidate))
				return candidate;
		}
		throw new MemberException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
	}
}
