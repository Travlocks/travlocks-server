package org.umc.travlocksserver.domain.template.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.constant.CityErrorCode;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.location.exception.CityException;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.template.dto.request.TemplatePreInputRequestDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplatePreInputResponseDTO;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.entity.TemplateCity;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.repository.TemplateCityRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.traveltheme.entity.TravelTheme;
import org.umc.travlocksserver.domain.traveltheme.repository.TravelThemeRepository;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.exception.TemplateException;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplatePreInputService {

	private final TemplateRepository templateRepository;
	private final MemberRepository memberRepository;
	private final CityRepository cityRepository;
	private final TravelThemeRepository travelThemeRepository;
	private final TemplateCityRepository templateCityRepository;
	private final TemplateDayRepository templateDayRepository;

	private static final String DEFAULT_TITLE = "새 여정";
	private static final LocalTime DEFAULT_START_TIME = LocalTime.of(9, 0);

	public TemplatePreInputResponseDTO createPreInput(
		Long memberId,
		TemplatePreInputRequestDTO request) {
		// 1. 회원 검증
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		// 2. City 검증
		List<City> cities = cityRepository.findAllById(request.destinationCityIds());
		if (cities.size() != request.destinationCityIds().size()) {
			throw new CityException(CityErrorCode.CITY_NOT_FOUND);
		}

		// 3. TravelTheme 검증
		// TODO: 현재 단일 테마만 지원. 향후 중간 테이블 추가 시 복수 테마 지원 필요
		List<TravelTheme> themes = travelThemeRepository.findAllById(request.travelThemeIds());
		if (themes.isEmpty()) {
			throw new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND);
		}
		TravelTheme firstTheme = themes.get(0); // 첫 번째 테마만 사용

		// 4. TransportType 변환
		// TODO: 현재 단일 교통수단만 지원. 향후 중간 테이블 추가 시 복수 교통수단 지원 필요
		TransportType transportType = TransportType.valueOf(request.transportTypes().get(0));

		// 5. Template 생성
		String shareToken = UUID.randomUUID().toString();

		Template template = Template.builder()
			.owner(member)
			.travelTheme(firstTheme)
			.title(DEFAULT_TITLE)
			.description(null)
			.coverImageUrl(null) // 랜덤 이미지는 나중에 추가
			.transportType(transportType)
			.progressRate(0)
			.tripDays(request.tripDays())
			.vlockCount(0)
			.isPublic(true)
			.shareToken(shareToken)
			.favoriteCount(0)
			.remixCount(0)
			.ratingCount(0)
			.avgRating(0.0)
			.build();

		Template savedTemplate = templateRepository.save(template);

		// 6. TemplateCity 생성 (복수)
		List<TemplateCity> templateCities = cities.stream()
			.map(city -> TemplateCity.builder()
				.template(savedTemplate)
				.city(city)
				.build())
			.toList();
		templateCityRepository.saveAll(templateCities);

		// 7. TemplateDay 생성 (days 수만큼)
		int days = request.tripDays().getDays();
		List<TemplateDay> templateDays = createTemplateDays(savedTemplate, days);
		templateDayRepository.saveAll(templateDays);

		// 8. Response 반환
		return TemplatePreInputResponseDTO.of(
			savedTemplate.getId(),
			days,
			shareToken);
	}

	/**
	 * TemplateDay 목록 생성
	 */
	private List<TemplateDay> createTemplateDays(Template template, int days) {
		return java.util.stream.IntStream.rangeClosed(1, days)
			.mapToObj(dayNo -> TemplateDay.builder()
				.template(template)
				.dayNo(dayNo)
				.startTime(DEFAULT_START_TIME)
				.vlockCount(0)
				.build())
			.toList();
	}
}
