package org.umc.travlocksserver.domain.template.service.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRemixResponseDTO;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.entity.TemplateCity;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.TemplateCityRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateRemixService {

	private final TemplateRepository templateRepository;
	private final MemberRepository memberRepository;
	private final TemplateCityRepository templateCityRepository;
	private final TemplateDayRepository templateDayRepository;
	private final TemplateVlockRepository templateVlockRepository;

	public TemplateRemixResponseDTO remix(Long templateId, Long remixerId) {
		Template original = templateRepository.findById(templateId)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

		Member remixer = memberRepository.findById(remixerId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		Template remixedTemplate = createRemixedTemplate(original, remixer);

		copyTemplateCities(original.getId(), remixedTemplate);

		Map<Long, TemplateDay> dayMap = copyTemplateDays(original.getId(), remixedTemplate);
		copyTemplateVlocks(dayMap);

		original.increaseRemixCount();

		return new TemplateRemixResponseDTO(original.getId(), remixedTemplate.getId());
	}

	/**
	 * Template 복제 + 생성
	 */
	private Template createRemixedTemplate(Template original, Member remixer) {
		Template remixed = Template.builder()
			// 복제
			.parentTemplate(original)
			.owner(remixer)
			.travelTheme(original.getTravelTheme())
			.title(original.getTitle() + " (리믹스)")
			.description(original.getDescription())
			.coverImageUrl(original.getCoverImageUrl())
			.transportType(original.getTransportType())
			.tripDays(original.getTripDays())
			.vlockCount(original.getVlockCount())

			// 초기화
			.progressRate(0)
			.startDate(null)
			.endDate(null)
			.isPublic(false)
			.shareToken(generateShareToken())
			.favoriteCount(0)
			.remixCount(0)
			.ratingCount(0)
			.avgRating(0.0)
			.build();

		return templateRepository.save(remixed);
	}

	/**
	 * TemplateCity 복제
	 */
	private void copyTemplateCities(Long originalTemplateId, Template newTemplate) {
		List<TemplateCity> originalCities = templateCityRepository.findByTemplateId(originalTemplateId);

		if (originalCities.isEmpty())
			return;

		List<TemplateCity> newCities = originalCities.stream()
			.map(tc -> TemplateCity.builder()
				.template(newTemplate)
				.city(tc.getCity())
				.build())
			.toList();

		templateCityRepository.saveAll(newCities);
	}

	/**
	 * TemplateDay 복제
	 */
	private Map<Long, TemplateDay> copyTemplateDays(Long originalTemplateId, Template newTemplate) {
		List<TemplateDay> originalDays = templateDayRepository.findByTemplateIdOrderByDayNoAsc(originalTemplateId);

		if (originalDays.isEmpty()) {
			return Map.of();
		}

		List<TemplateDay> newDays = originalDays.stream()
			.map(od -> TemplateDay.builder()
				.template(newTemplate)
				.dayNo(od.getDayNo())
				.startTime(od.getStartTime())
				.vlockCount(od.getVlockCount())
				.build())
			.toList();

		List<TemplateDay> savedDays = templateDayRepository.saveAll(newDays);

		Map<Long, TemplateDay> dayMap = new HashMap<>();
		for (int i = 0; i < originalDays.size(); i++) {
			dayMap.put(originalDays.get(i).getId(), savedDays.get(i));
		}
		return dayMap;
	}

	/**
	 * TemplateVlock 복제
	 */
	private void copyTemplateVlocks(Map<Long, TemplateDay> dayMap) {
		if (dayMap.isEmpty())
			return;

		List<Long> originalDayIds = new ArrayList<>(dayMap.keySet());
		List<TemplateVlock> originalVlocks = templateVlockRepository.findByTemplateDayIdIn(originalDayIds);

		if (originalVlocks.isEmpty())
			return;

		List<TemplateVlock> newVlocks = originalVlocks.stream()
			.map(ov -> TemplateVlock.builder()
				.templateDay(dayMap.get(ov.getTemplateDay().getId()))
				.vlock(ov.getVlock())
				.orderNo(ov.getOrderNo())
				.stayMinutes(ov.getStayMinutes())
				.build())
			.toList();

		templateVlockRepository.saveAll(newVlocks);
	}

	/**
	 * 토큰 생성
	 */
	private String generateShareToken() {
		return java.util.UUID.randomUUID().toString();
	}
}
