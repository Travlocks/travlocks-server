package org.umc.travlocksserver.domain.template.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.favorite.repository.FavoriteRepository;
import org.umc.travlocksserver.domain.template.code.TemplateErrorCode;
import org.umc.travlocksserver.domain.template.dto.response.PopularTemplateResponse;
import org.umc.travlocksserver.domain.template.dto.response.TemplateDetailResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationCardDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateRecommendationsDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateExploreResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.*;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.enums.TripDays;
import org.umc.travlocksserver.domain.template.exception.TemplateException;
import org.umc.travlocksserver.domain.template.repository.TemplateExploreRepositoryCustom;
import org.umc.travlocksserver.domain.template.repository.TemplateRepository;
import org.umc.travlocksserver.domain.traveltheme.repository.PreferredTravelThemeRepository;
import org.umc.travlocksserver.infra.redis.template.CachedTemplateRecommendations;
import org.umc.travlocksserver.infra.redis.template.TemplateRecommendationCache;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TemplateQueryService {

	private static final int RECENT_TEMPLATE_LIMIT = 5;
	private static final int RECOMMEND_TEMPLATE_LIMIT = 10;

	private final TemplateRecommendationCache cache;
	private final PreferredTravelThemeRepository preferredTravelThemeRepository;
	private final TemplateRepository templateRepository;
	private final FavoriteRepository favoriteRepository;
	private final TemplateExploreRepositoryCustom templateExploreRepositoryCustom;
	private final TemplateTagQueryService templateTagQueryService;

	public TemplateRecommendationsDTO getRecommendedTemplates(Long memberId) {
		CachedTemplateRecommendations cached = cache.get(memberId);

		// Cache hit -> Caching된 추천 리스트 반환
		if (cached != null) {
			log.info("캐싱된 데이터 반환");
			return TemplateRecommendationsDTO.from(cached);
		}

		// Cache miss -> 추천
		log.info("새로운 추천 데이터 생성");
		List<TemplateRecommendationCardDTO> templates = recommendTemplates(memberId);
		CachedTemplateRecommendations recommendedTemplates = CachedTemplateRecommendations.from(templates);
		cache.set(memberId, recommendedTemplates);
		return TemplateRecommendationsDTO.from(recommendedTemplates);
	}

	private List<TemplateRecommendationCardDTO> recommendTemplates(Long memberId) {
		// 회원 선호 테마 ID들 조회
		List<Long> preferredThemeIds = preferredTravelThemeRepository.findPreferredThemeIdsByMemberId(memberId);

		// 최근 5개 템플릿의 TravleThemeID 뽑고 Distinct
		List<Long> recentThemeIds = templateRepository.findRecentThemeIdsByMemberId(memberId,
			PageRequest.of(0, RECENT_TEMPLATE_LIMIT));

		// 이미 리믹스한 템플릿 ID들 조회 (추천에서 제외하기 위함)
		List<Long> excludedTemplateIds = templateRepository.findRemixedTemplateIdsByMemberId(memberId);

		// 개인화 추천
		List<TemplateRecommendationCardDTO> result = templateRepository.recommendPersonalized(preferredThemeIds,
			recentThemeIds, excludedTemplateIds, RECOMMEND_TEMPLATE_LIMIT);

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

		if (!template.getIsPublic()) {
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

	public List<TemplateExploreResponseDTO> exploreTemplates(
		String keyword,
		List<String> cities,
		List<String> themes,
		List<TripDays> tripDays,
		List<String> transportTypes,
		String sort,
		int page) {

		List<TemplateExploreResponseDTO> result = templateExploreRepositoryCustom.findExploreTemplates(
			keyword,
			cities,
			themes,
			tripDays,
			transportTypes,
			sort,
			page * 9);

		if (result.isEmpty()) {
			throw new TemplateException(TemplateErrorCode.TEMPLATE_NO_MATCH);
		}

		return result;
	}

	public List<TemplateLatestDTO> getRecentTemplates(Long memberId) {
		List<Template> templates = templateRepository.findRecentTemplatesByOwner(memberId);

		List<TemplateLatestDTO> dtos = templates.stream()
			.limit(2) // 최신 2개만
			.map(t -> {
				String regionName = t.getTemplateCities().stream()
					.findFirst()
					.map(tc -> tc.getCity().getRegion().getName())
					.orElse(null);

				return new TemplateLatestDTO(
					t.getId(),
					t.getTitle(),
					t.getUpdatedAt(),
					t.getProgressRate(),
					regionName);
			})
			.toList();

		if (dtos.isEmpty()) {
			throw new TemplateException(TemplateErrorCode.TEMPLATE_RECENT_NOT_FOUND);
		}

		return dtos;
	}

	public Template getTemplateByIdAndOwnerId(Long templateId, Long memberId) {
		return templateRepository.findByIdAndOwnerId(templateId, memberId)
			.orElseThrow(() -> new TemplateException(TemplateErrorCode.TEMPLATE_NOT_FOUND));
	}
}
