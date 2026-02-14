package org.umc.travlocksserver.domain.member.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberQueryService {

	private final MemberRepository memberRepository;

	public Member getById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
	}
}
