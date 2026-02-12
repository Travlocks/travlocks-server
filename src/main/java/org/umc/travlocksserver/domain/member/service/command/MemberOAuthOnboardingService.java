package org.umc.travlocksserver.domain.member.service.command;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.auth.service.command.AuthService;
import org.umc.travlocksserver.domain.member.dto.request.MemberOAuthOnboardingRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.enums.MemberStatus;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberOAuthOnboardingService {
	private final MemberRepository memberRepository;
	private final AuthService authService;
	private final MemberConsentPreferenceProcessor processor;

	@Transactional
	public MemberSignupResponseDTO completeOAuthOnboarding(
		Member member,
		MemberOAuthOnboardingRequestDTO request,
		HttpServletResponse response) {
		if (member.getStatus() != MemberStatus.ONBOARDING) {
			throw new MemberException(MemberErrorCode.INVALID_ONBOARDING_STATUS);
		}

		if (memberRepository.existsByNicknameAndIdNot(request.nickname(), member.getId())) {
			throw new MemberException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
		}

		member.changeNickname(request.nickname());
		member.changeStatusActive();

		// 약관/선호 처리
		var result = processor.process(
			member,
			request.consents().stream()
				.map(c -> new MemberConsentPreferenceProcessor.ConsentInput(c.policyId(), c.agreed()))
				.toList(),
			request.preferredTravelStyleIds(),
			request.preferredTravelThemeIds(),
			true);

		// 토큰 발급
		AuthService.IssuedTokens tokens = authService.issueTokens(member.getId(), response);

		return new MemberSignupResponseDTO(
			member.getId(),
			member.getNickname(),
			tokens.accessToken(),
			tokens.accessTokenExpiresIn(),
			member.getProfileImageUrl(),
			result.preferredThemes(),
			result.preferredStyles());
	}
}
