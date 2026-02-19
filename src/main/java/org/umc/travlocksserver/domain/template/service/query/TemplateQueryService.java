package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.favorite.repository.FavoriteRepository;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDetailResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateSuggestionCardDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateSuggestionsDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateExploreResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.*;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.enums.TripDays;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.TemplateExploreRepositoryCustom;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.traveltheme.repository.PreferredTravelThemeRepository;
import org.umc.travlocksserver.global.response.PageResponseDTO;
import org.umc.travlocksserver.infra.redis.template.CachedTemplateSuggestions;
import org.umc.travlocksserver.infra.redis.template.TemplateSuggestionCache;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TemplateQueryService {

	@Value("${template.limit.recent}")
	private int recentTemplateLimit;

	@Value("${template.limit.suggestion}")
	private int suggestionTemplateLimit;

	private final TemplateSuggestionCache cache;
	private final PreferredTravelThemeRepository preferredTravelThemeRepository;
	private final TemplateRepository templateRepository;
	private final FavoriteRepository favoriteRepository;
	private final TemplateExploreRepositoryCustom templateExploreRepositoryCustom;
	private final TemplateTagQueryService templateTagQueryService;

	public TemplateSuggestionsDTO getSuggestedTemplates(Long memberId) {
		CachedTemplateSuggestions cached = cache.get(memberId);

		// Cache hit -> Caching된 추천 리스트 반환
		if (cached != null) {
			return TemplateSuggestionsDTO.from(cached);
		}

		// Cache miss -> 추천
		List<TemplateSuggestionCardDTO> templates = suggestTemplates(memberId);
		CachedTemplateSuggestions suggestedTemplates = CachedTemplateSuggestions.from(templates);
		cache.set(memberId, suggestedTemplates);
		return TemplateSuggestionsDTO.from(suggestedTemplates);
	}

	private List<TemplateSuggestionCardDTO> suggestTemplates(Long memberId) {
		// 회원 선호 테마 ID들 조회
		List<Long> preferredThemeIds = preferredTravelThemeRepository.findPreferredThemeIdsByMemberId(memberId);

		// 최근 5개 템플릿의 TravleThemeID 뽑고 Distinct
		List<Long> recentThemeIds = templateRepository.findRecentThemeIdsByMemberId(memberId,
			PageRequest.of(0, recentTemplateLimit));

		// 이미 리믹스한 템플릿 ID들 조회 (추천에서 제외하기 위함)
		List<Long> excludedTemplateIds = templateRepository.findRemixedTemplateIdsByMemberId(memberId);

		// 개인화 추천
		List<TemplateSuggestionCardDTO> result = templateRepository.suggestPersonalized(preferredThemeIds,
			recentThemeIds, excludedTemplateIds, suggestionTemplateLimit);

		return result;
	}

	public List<PopularTemplateResponse> getPopularTemplates(int limit) {

		return templateRepository.findPopularTemplates(PageRequest.of(0, limit))
			.stream()
			.map(this::toResponse)
			.toList();
	}

	private PopularTemplateResponse toResponse(Template template) {
		return PopularTemplateResponse.builder()
			.templateId(template.getId())
			.coverImageUrl(template.getCoverImageUrl())
			.title(template.getTitle())
			.avgRating(template.getAvgRating())
			.remixCount(template.getRemixCount())
			.travelTheme(template.getTravelTheme().getContent())
			.ownerNickname(template.getOwner().getNickname())
			.build();
	}

	public TemplateDetailResponseDTO getTemplateDetail(Long templateId, Long memberId) {
		Template template = templateRepository.findById(templateId)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

		if (!template.getIsPublic() && !template.getOwner().getId().equals(memberId)) {
			throw new TemplateException(TemplateErrorCode.TEMPLATE_NOT_PUBLIC);
		}

		// 즐겨찾기 여부
		boolean isFavorited = false;
		if (memberId != null) {
			isFavorited = favoriteRepository
				.existsByMemberIdAndTemplateId(memberId, templateId);
		}

		// 태그 목록
		List<String> tags = templateTagQueryService.getTemplateTags(templateId,template.getTagVersion());

		// 블록 목록
		List<TemplateDetailResponseDTO.VlockDTO> blocks = template.getTemplateDays().stream()
			.flatMap(day -> day.getTemplateVlocks().stream())
			.map(tv -> new TemplateDetailResponseDTO.VlockDTO(
				tv.getVlock().getId(),
				tv.getVlock().getName(),
				tv.getVlock().getLatitude(),
				tv.getVlock().getLongitude(),
				tv.getVlock().getAddress()))
			.toList();

		// 도시명 (첫 번째 도시만)
		String cityName = template.getTemplateCities().stream()
			.map(tc -> tc.getCity().getName())
			.findFirst()
			.orElse("");

		return new TemplateDetailResponseDTO(
			template.getId(),
			template.getTitle(),
			cityName,
			template.getTravelTheme().getContent(),
			template.getOwner().getProfileImageUrl(),
			template.getOwner().getNickname(),
			template.getCoverImageUrl(),
			template.getOwner().getId(),
			template.getAvgRating(),
			template.getTripDays().getDescription(),
			template.getRemixCount(),
			template.getDescription(),
			tags,
			blocks,
			isFavorited);
	}

	public PageResponseDTO<TemplateExploreResponseDTO> exploreTemplatesWithPage(
		String keyword,
		List<String> cities,
		List<String> themes,
		List<TripDays> tripDays,
		List<String> transportTypes,
		String sort,
		Pageable pageable) {

		Page<TemplateExploreResponseDTO> result = templateExploreRepositoryCustom.findExploreTemplatesWithPage(
				keyword, cities, themes, tripDays, transportTypes, sort, pageable);

		if (result.isEmpty()) {
			throw new TemplateException(TemplateErrorCode.TEMPLATE_NO_MATCH);
		}

		return PageResponseDTO.from(result);
	}

	/**
	 * 최근 편집 초안 조회 (홈 화면용)
	 * 초안만 필터링하고, 진행률을 실시간 계산하여 반환
	 */
	public List<TemplateLatestDTO> getRecentTemplates(Long memberId) {
		List<Template> templates = templateRepository.findRecentTemplatesByOwner(memberId);

		// 초안만 필터링 후 최대 2개
		List<TemplateLatestDTO> dtos = templates.stream()
				.filter(this::isDraft)  // 초안만
				.limit(2)
				.map(t -> {
					String regionName = t.getTemplateCities().stream()
							.findFirst()
							.map(tc -> tc.getCity().getRegion().getName())
							.orElse(null);

					// 실시간 진행률 계산
					int progressRate = calculateProgressRate(t);

					return new TemplateLatestDTO(
							t.getId(),
							t.getTitle(),
							t.getUpdatedAt(),
							progressRate,
							regionName);
				})
				.toList();

		// 초안이 없어도 빈 리스트 반환 (에러 X)
		return dtos;
	}

	/**
	 * 초안 여부 판정
	 * 다음 중 하나라도 만족하면 초안:
	 * 1. 전체 블록 ≤ 2개
	 * 2. 블록 0개인 Day 존재
	 * 3. 모든 블록이 1일차에만 몰려있음
	 */
	public boolean isDraft(Template template) {
		List<TemplateDay> days = template.getTemplateDays();

		// 총 블록 수
		int totalVlocks = days.stream()
				.mapToInt(TemplateDay::getVlockCount)
				.sum();

		// 조건 1: 전체 블록 ≤ 2개
		if (totalVlocks <= 2) {
			return true;
		}

		// 조건 2: 블록 0개인 Day 존재
		boolean hasEmptyDay = days.stream()
				.anyMatch(day -> day.getVlockCount() == 0);
		if (hasEmptyDay) {
			return true;
		}

		// 조건 3: 모든 블록이 1일차에만 몰려있음
		if (days.size() > 1 && totalVlocks >= 1) {
			long daysWithVlocks = days.stream()
					.filter(day -> day.getVlockCount() > 0)
					.count();

			if (daysWithVlocks == 1 && days.get(0).getVlockCount() == totalVlocks) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 진행률 계산
	 * Day 완성도 (60점) + 블록 밀도 (40점)
	 * 20단위 내림
	 */
	public int calculateProgressRate(Template template) {
		List<TemplateDay> days = template.getTemplateDays();
		int totalDays = days.size();

		if (totalDays == 0) {
			return 0;
		}

		// 1. Day 완성도 계산 (최대 60점)
		long daysWithVlocks = days.stream()
				.filter(day -> day.getVlockCount() > 0)
				.count();

		double dayCompletionRatio = (double) daysWithVlocks / totalDays;
		int dayScore = getDayScore(dayCompletionRatio);

		// 2. 블록 밀도 계산 (최대 40점)
		int totalVlocks = days.stream()
				.mapToInt(TemplateDay::getVlockCount)
				.sum();

		int recommendedVlocks = totalDays * 2;
		double densityRatio = (double) totalVlocks / recommendedVlocks;
		int densityScore = getDensityScore(densityRatio);

		// 3. 최종 진행률 (20단위 내림)
		int totalScore = dayScore + densityScore;
		return (totalScore / 20) * 20;
	}

	private int getDayScore(double ratio) {
		if (ratio >= 0.75) return 60;  // 75% 이상
		if (ratio >= 0.50) return 40;  // 50~74%
		if (ratio >= 0.01) return 20;  // 1~49%
		return 0;
	}

	private int getDensityScore(double ratio) {
		if (ratio >= 1.00) return 40;  // 100% 이상
		if (ratio >= 0.75) return 30;  // 75~99%
		if (ratio >= 0.50) return 20;  // 50~74%
		if (ratio >= 0.01) return 10;  // 1~49%
		return 0;
	}

	public Template getTemplateByIdAndOwnerId(Long templateId, Long memberId) {
		return templateRepository.findByIdAndOwnerId(templateId, memberId)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));
	}
}
