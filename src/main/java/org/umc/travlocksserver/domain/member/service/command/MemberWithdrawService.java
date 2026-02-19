package org.umc.travlocksserver.domain.member.service.command;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.auth.repository.OAuthAccountRepository;
import org.umc.travlocksserver.domain.auth.service.command.AuthService;
import org.umc.travlocksserver.domain.favorite.repository.FavoriteRepository;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.entity.MemberDeletionLog;
import org.umc.travlocksserver.domain.member.repository.MemberConsentRepository;
import org.umc.travlocksserver.domain.member.repository.MemberDeletionLogRepository;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateRatingRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.travelstyle.repository.PreferredTravelStyleRepository;
import org.umc.travlocksserver.domain.traveltheme.repository.PreferredTravelThemeRepository;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;

@Service
@RequiredArgsConstructor
public class MemberWithdrawService {
	private static final Long DELETED_MEMBER_ID = 999_999L;

	private final MemberRepository memberRepository;
	private final MemberDeletionLogRepository memberDeletionLogRepository;
	private final PreferredTravelStyleRepository preferredTravelStyleRepository;
	private final PreferredTravelThemeRepository preferredTravelThemeRepository;
	private final MemberConsentRepository memberConsentRepository;
	private final FavoriteRepository starRepository;
	private final OAuthAccountRepository oAuthAccountRepository;
	private final VlockRepository vlockRepository;
	private final TemplateRepository templateRepository;
	private final TemplateRatingRepository templateRatingRepository;
	private final AuthService authService;

	@Transactional
	public void withdraw(Member loginMember, String reason, HttpServletRequest request, HttpServletResponse response) {
		Long memberId = loginMember.getId();

		if (!memberRepository.existsById(DELETED_MEMBER_ID)) {
			throw new IllegalStateException("DELETED_MEMBER 더미 계정이 DB에 없습니다. id=" + DELETED_MEMBER_ID);
		}

		memberDeletionLogRepository.save(
			MemberDeletionLog.create(loginMember, reason));

		authService.invalidateRefreshToken(request, response);

		preferredTravelStyleRepository.deleteByMemberId(memberId);
		preferredTravelThemeRepository.deleteByMemberId(memberId);
		memberConsentRepository.deleteByMemberId(memberId);
		starRepository.deleteByMemberId(memberId);
		oAuthAccountRepository.deleteByMemberId(memberId);

		vlockRepository.transferOwner(memberId, DELETED_MEMBER_ID);
		templateRepository.transferOwner(memberId, DELETED_MEMBER_ID);
		templateRatingRepository.transferRater(memberId, DELETED_MEMBER_ID);

		// managed 상태로 복구
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));

		member.withdrawAndAnonymize(
			anonymizedNickname(memberId));
	}

	private String anonymizedNickname(Long memberId) {
		String suffix = String.valueOf(memberId);
		if (suffix.length() > 7) {
			suffix = suffix.substring(suffix.length() - 7);
		}
		return "탈퇴" + suffix;
	}

}
