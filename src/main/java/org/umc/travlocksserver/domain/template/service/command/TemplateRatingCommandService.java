package org.umc.travlocksserver.domain.template.service.command;

import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.dto.request.TemplateRatingCreateRequestDTO;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.entity.TemplateRating;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.TemplateRatingRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateRatingCommandService {

	private final TemplateRepository templateRepository;
	private final MemberRepository memberRepository;
	private final TemplateRatingRepository templateRatingRepository;

	public void createTemplateRating(Long templateId, Long memberId, TemplateRatingCreateRequestDTO request) {

		Template template = templateRepository.findById(templateId)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		TemplateRating ratingEntity = TemplateRating.builder()
			.template(template)
			.member(member)
			.rating(request.rating())
			.content(request.content())
			.build();

		templateRatingRepository.saveAndFlush(ratingEntity);

		// 평균 재집계 후 템플릿에 반영
		Double avg = templateRatingRepository.findAvgRatingByTemplateId(templateId);
		template.updateAvgRating(avg);
	}
}