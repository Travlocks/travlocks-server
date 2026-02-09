package org.umc.travlocksserver.domain.member.service.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.member.dto.response.MemberProfileResponseDTO;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.template.dto.response.TemplateCardResponseDTO;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.global.response.PageResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileQueryService {

	private final MemberRepository memberRepository;
	private final TemplateRepository templateRepository;

	public MemberProfileResponseDTO getMemberProfile(Long memberId, Pageable pageable) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		Page<TemplateCardResponseDTO> page = templateRepository.findPublicTemplateCardsByOwnerId(memberId, pageable);

		return new MemberProfileResponseDTO(
			member.getId(),
			member.getNickname(),
			member.getIntroduction(),
			member.getProfileImageUrl(),
			PageResponseDTO.from(page)
		);
	}
}
