package org.umc.travlocksserver.domain.template.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.dto.response.VlockSuggestionsResponseDTO;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.exception.TemplateDayException;
import org.umc.travlocksserver.domain.template.code.TemplateDayErrorCode;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;
import org.umc.travlocksserver.domain.template.service.query.TemplateCityQueryService;
import org.umc.travlocksserver.domain.template.service.query.TemplateQueryService;
import org.umc.travlocksserver.domain.template.service.query.TemplateVlockQueryService;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.service.command.VlockExternalCommandService;
import org.umc.travlocksserver.domain.vlock.service.query.VlockQueryService;
import org.umc.travlocksserver.global.aws.S3Properties;
import org.umc.travlocksserver.global.geo.BoundingBox;
import org.umc.travlocksserver.global.geo.GeoUtil;
import org.umc.travlocksserver.global.geo.LatLng;
import org.umc.travlocksserver.infra.ai.client.AiSuggestionClient;
import org.umc.travlocksserver.infra.ai.dto.ScoredCandidate;
import org.umc.travlocksserver.infra.redis.vlock.CachedVlockSuggestions;
import org.umc.travlocksserver.infra.redis.vlock.VlockSuggestionCache;

import org.umc.travlocksserver.domain.template.dto.request.TemplateVlockAddRequestDTO;
import org.umc.travlocksserver.domain.template.dto.request.TemplateVlockReorderRequestDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateVlockAddResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateVlockDeleteResponseDTO;
import org.umc.travlocksserver.domain.template.dto.response.TemplateVlockReorderResponseDTO;
import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.vlock.code.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TemplateDayCommandService {

	private final TemplateDayRepository templateDayRepository;
	private final TemplateVlockRepository templateVlockRepository;
	private final VlockRepository vlockRepository;

	private final TemplateCityQueryService templateCityQueryService;
	private final VlockExternalCommandService vlockExternalCommandService;
	private final TemplateVlockQueryService templateVlockQueryService;
	private final VlockQueryService vlockQueryService;
	private final TemplateQueryService templateQueryService;

	private final VlockSuggestionCache vlockSuggestionCache;

	private final AiSuggestionClient aiClient;

	private final S3Properties s3Properties;

	@Value("${suggestion.vlock.popular-pool}")
	private int popularPool;

	@Value("${suggestion.vlock.min-pool}")
	private int minPool;

	@Value("${suggestion.vlock.box-pool}")
	private int boxPool;

	@Value("${suggestion.vlock.ai-candidate-pool}")
	private int aiCandidatePool;

	@Value("${suggestion.vlock.pool-size}")
	private int poolSize;

	@Value("${suggestion.vlock.size}")
	private int vlockSuggestionSize;

	@Value("${suggestion.vlock.max-duplicate-category}")
	private int maxDuplicateCategory;

	/**
	 * Vlock 추천 결과를 반환하는 메서드
	 */

	// 블록 추가
	public TemplateVlockAddResponseDTO addVlock(
		Long memberId,
		Long templateId,
		Integer dayNo,
		TemplateVlockAddRequestDTO request) {
		// 1. TemplateDay 조회 및 권한 검증
		TemplateDay templateDay = templateDayRepository
			.findTemplateDayAccesible(memberId, templateId, dayNo)
			.orElseThrow(() -> new TemplateDayException(TemplateDayErrorCode.TEMPLATE_DAY_NOT_FOUND));

		// 2. Vlock 조회
		Vlock vlock = vlockRepository.findById(request.vlockId())
			.orElseThrow(() -> new VlockException(VlockErrorCode.VLOCK_NOT_FOUND));

		// 3. 현재 마지막 orderNo 조회
		Integer maxOrderNo = templateVlockRepository
			.findByTemplateDayIdOrderByOrderNo(templateDay.getId())
			.stream()
			.map(TemplateVlock::getOrderNo)
			.max(Integer::compareTo)
			.orElse(0);

		// 4. TemplateVlock 생성
		TemplateVlock templateVlock = TemplateVlock.builder()
			.templateDay(templateDay)
			.vlock(vlock)
			.orderNo(maxOrderNo + 1)
			.stayHours(vlock.getVlockCategory().getStayHours())
			.canvasX(request.canvasX())
			.canvasY(request.canvasY())
			.inputPort(request.inputPort())
			.outputPort(request.outputPort())
			.build();

		templateVlockRepository.save(templateVlock);

		// 5. vlockCount 증가
		templateDay.incrementVlockCount();

		// 6. 경고 메시지 생성
		String warning = null;
		if (templateDay.getVlockCount() > 4) {
			warning = "하루 권장 블록 개수 4개가 초과되었습니다.";
		}

		return TemplateVlockAddResponseDTO.from(templateVlock, warning);
	}

	// 블록 삭제
	public TemplateVlockDeleteResponseDTO deleteVlock(
		Long memberId,
		Long templateId,
		Integer dayNo,
		Long templateVlocksId) {
		// 1. TemplateDay 조회 및 권한 검증
		TemplateDay templateDay = templateDayRepository
			.findTemplateDayAccesible(memberId, templateId, dayNo)
			.orElseThrow(() -> new TemplateDayException(TemplateDayErrorCode.TEMPLATE_DAY_NOT_FOUND));

		// 2. TemplateVlock 조회
		TemplateVlock templateVlock = templateVlockRepository.findById(templateVlocksId)
			.orElseThrow(() -> new TemplateDayException(TemplateDayErrorCode.TEMPLATE_VLOCK_NOT_FOUND));

		// 3. 권한 확인 (해당 day의 블록인지)
		if (!templateVlock.getTemplateDay().getId().equals(templateDay.getId())) {
			throw new TemplateDayException(TemplateDayErrorCode.TEMPLATE_VLOCK_NOT_FOUND);
		}

		// 4. 삭제
		templateVlockRepository.delete(templateVlock);

		// 5. 남은 블록들 조회 및 orderNo 재정렬
		List<TemplateVlock> remainingVlocks = templateVlockRepository
			.findByTemplateDayIdOrderByOrderNo(templateDay.getId());

		for (int i = 0; i < remainingVlocks.size(); i++) {
			remainingVlocks.get(i).updateOrderNo(i + 1);
		}

		if (!remainingVlocks.isEmpty()) {
			templateVlockRepository.saveAll(remainingVlocks);
		}

		// 6. vlockCount 감소
		templateDay.decrementVlockCount();

		return TemplateVlockDeleteResponseDTO.from(
			templateVlocksId,
			templateDay.getId(),
			dayNo,
			remainingVlocks);
	}

	// 블록 순서 변경
	public TemplateVlockReorderResponseDTO reorderVlocks(
		Long memberId,
		Long templateId,
		Integer dayNo,
		TemplateVlockReorderRequestDTO request) {
		// 1. TemplateDay 조회 및 권한 검증
		TemplateDay templateDay = templateDayRepository
			.findTemplateDayAccesible(memberId, templateId, dayNo)
			.orElseThrow(() -> new TemplateDayException(TemplateDayErrorCode.TEMPLATE_DAY_NOT_FOUND));

		// 2. 모든 TemplateVlock 조회
		List<Long> ids = request.vlockOrders().stream()
			.map(TemplateVlockReorderRequestDTO.VlockOrder::templateVlocksId)
			.toList();

		List<TemplateVlock> templateVlocks = templateVlockRepository.findAllById(ids);

		if (templateVlocks.size() != ids.size()) {
			throw new TemplateDayException(TemplateDayErrorCode.TEMPLATE_VLOCK_NOT_FOUND);
		}

		// 3. 모든 블록이 해당 day에 속하는지 검증
		boolean allBelongToDay = templateVlocks.stream()
			.allMatch(tv -> tv.getTemplateDay().getId().equals(templateDay.getId()));

		if (!allBelongToDay) {
			throw new TemplateDayException(TemplateDayErrorCode.TEMPLATE_VLOCK_NOT_FOUND);
		}

		// 4. orderNo, 레이아웃 정보 일괄 업데이트
		Map<Long, TemplateVlock> vlockMap = templateVlocks.stream()
			.collect(Collectors.toMap(TemplateVlock::getId, v -> v));

		for (var order : request.vlockOrders()) {
			TemplateVlock tv = vlockMap.get(order.templateVlocksId());
			tv.updateOrderNo(order.orderNo());
			tv.updateLayout(
				order.canvasX(),
				order.canvasY(),
				order.inputPort(),
				order.outputPort());
		}

		templateVlockRepository.saveAll(templateVlocks);

		// 5. 정렬된 순서로 재조회
		List<TemplateVlock> orderedVlocks = templateVlockRepository
			.findByTemplateDayIdOrderByOrderNo(templateDay.getId());

		// 6. 경고 메시지 생성 (TODO: 이동시간 계산 로직 추가 가능)
		List<String> warnings = List.of();

		return TemplateVlockReorderResponseDTO.from(
			templateDay.getId(),
			dayNo,
			orderedVlocks,
			warnings);
	}

	public VlockSuggestionsResponseDTO suggestVlocks(Long memberId, Long templateId) {
		long seed = System.currentTimeMillis();

		// Template 존재 및 권한 검증
		Template template = templateQueryService.getTemplateByIdAndOwnerId(templateId, memberId);

		// 캐시 hit
		CachedVlockSuggestions cached = vlockSuggestionCache.get(templateId);
		if (cached != null && cached.vlockIds() != null && cached.vlockIds().size() >= 3) {
			List<VlockSuggestionsResponseDTO.VlockSuggestionCardDTO> cards = pick3FromPoolAndUpdateRecent(templateId,
				cached, seed);
			return VlockSuggestionsResponseDTO.from(cards, seed);
		}

		// 캐시 miss
		CachedVlockSuggestions newCached = buildSuggestion(template);
		if (newCached.vlockIds() == null || newCached.vlockIds().size() < 3) {
			return VlockSuggestionsResponseDTO.from(List.of(), seed);
		}

		vlockSuggestionCache.set(templateId, newCached);

		List<VlockSuggestionsResponseDTO.VlockSuggestionCardDTO> cards = pick3FromPoolAndUpdateRecent(templateId,
			newCached, seed);
		return VlockSuggestionsResponseDTO.from(cards, seed);
	}

	/**
	 * Redis에 저장할 추천 블록을 만드는 메서드
	 */
	private CachedVlockSuggestions buildSuggestion(Template template) {
		Long templateId = template.getId();
		TransportType transportType = template.getTransportType();
		List<Long> cityIdsOfTemplate = templateCityQueryService.getCityIdsByTemplateId(templateId);

		// 해당 템플릿에서 사용중인 블록 조회 (중복 제외)
		List<Vlock> usedVlocksInTemplate = templateVlockQueryService.getDistinctVlocksByTemplateId(templateId);

		// 해당 템플릿에서 사용중인 블록 ID 조회 (중복 제외)
		Set<Long> usedVlockIdsInTemplate = usedVlocksInTemplate.stream()
			.map(Vlock::getId)
			.collect(Collectors.toSet());

		// 추천 블록 후보
		List<Vlock> candidates;

		// 현재 Template에 들어간 블록들의 중심 좌표
		LatLng center = usedVlocksInTemplate.isEmpty() ? null : LatLng.averageFrom(usedVlocksInTemplate);

		// 추천 블록 후보 조회 또는 추가
		candidates = vlockQueryService.getPopularByCityIds(cityIdsOfTemplate, PageRequest.of(0, popularPool));

		// 블록 추천 후보 수가 minPool 이하면 외부 API(카카오맵)에서 가져와서 저장
		if (candidates.size() < minPool) {
			vlockExternalCommandService.fetchFromExternal(templateId, null, null);
			candidates = vlockQueryService.getPopularByCityIds(cityIdsOfTemplate, PageRequest.of(0, popularPool));
		}

		// 템플릿 내에 이미 존재하는 블록은 추천 후보에서 제거
		List<Vlock> filtered = candidates.stream()
			.filter(v -> !usedVlockIdsInTemplate.contains(v.getId()))
			.toList();

		// 후보 선정
		List<Vlock> aiCandidates = pickAiCandidates(filtered, usedVlocksInTemplate, center);

		// AI 적합도 점수
		Map<Long, Double> aiScores = aiClient.suggestVlocks(usedVlocksInTemplate, aiCandidates);

		// Template에 들어간 vlock들의 카테고리 종류 목록
		Set<String> categoriesInTemplate = usedVlocksInTemplate.stream()
			.map(v -> v.getVlockCategory().getName())
			.collect(Collectors.toSet());

		List<ScoredCandidate> scored = new ArrayList<>(aiCandidates.size());

		for (Vlock v : aiCandidates) {
			// AI 적합도
			double aiScore = aiScores.getOrDefault(v.getId(), 0.5);

			// 이동 시간 적합도
			double moveScore = (center == null)
				? 0.6
				: travelTimeScoreFromDistance(
					GeoUtil.haversineKm(center, new LatLng(v.getLatitude(), v.getLongitude())),
					transportType);

			// 체류 적합도
			double stayScore = calculateStayScore(usedVlocksInTemplate, v);

			// 장소 신뢰도
			double reliableScore = calculateReliableScore(v.getUsageCount());

			// 다양성 보정
			double diversityScore = calculateDiversityScore(categoriesInTemplate, v.getVlockCategory().getName());

			double finalScore = aiScore * 0.35 + moveScore * 0.30 + stayScore * 0.20 + reliableScore + 0.10
				+ diversityScore * 0.05;

			scored.add(ScoredCandidate.of(v, finalScore));
		}

		List<Long> poolIds = buildPoolIds(scored, poolSize);

		return CachedVlockSuggestions.of(poolIds);
	}

	/**
	 * 카테고리 중복 제한 수를 고려해 추천 Vlock들을 구성하는 메서드
	 */
	private List<Long> buildPoolIds(List<ScoredCandidate> scored, int poolSize) {

		List<ScoredCandidate> sorted = scored.stream()
			.sorted(Comparator.comparingDouble(ScoredCandidate::score).reversed())
			.toList();

		Map<String, Integer> categoryCount = new HashMap<>();
		List<Long> picked = new ArrayList<>(poolSize);

		for (ScoredCandidate sc : sorted) {
			if (picked.size() >= poolSize)
				break;

			int c = categoryCount.getOrDefault(sc.vlock().getVlockCategory().getName(), 0);
			if (c >= maxDuplicateCategory)
				continue;

			picked.add(sc.vlock().getId());
			categoryCount.put(sc.vlock().getVlockCategory().getName(), c + 1);
		}

		if (picked.size() < Math.min(poolSize, sorted.size())) {
			for (ScoredCandidate sc : sorted) {
				if (picked.size() >= poolSize)
					break;
				if (picked.contains(sc.vlock().getId()))
					continue;
				picked.add(sc.vlock().getId());
			}
		}

		return picked;
	}

	/**
	 * 다양성 적합도 점수를 계산하는 메서드
	 * 현재 템플릿에 없는 카테고리면 1.0, 있으면 0.3
	 */
	private double calculateDiversityScore(Set<String> categoriesInDay, String candidateCategory) {
		return categoriesInDay.contains(candidateCategory) ? 0.3 : 1.0;
	}

	/**
	 * 장소 신뢰도 점수를 계산하는 메서드 (usageCount 기반)
	 * - 0개 -> 0.1
	 * - 1~3개 -> 0.4,
	 * - 4~10개 -> 0.7,
	 * - 11개 이상 -> 1.0
	 */
	private double calculateReliableScore(Integer usageCount) {
		if (usageCount <= 0)
			return 0.1;
		if (usageCount <= 3)
			return 0.4;
		if (usageCount <= 10)
			return 0.7;
		return 1.0;
	}

	/**
	 * 체류 적합도 점수를 계산하는 메서드 (기존 블록수 및 후보 체류시간 기반)
	 * - 0~1개: 1~2시간 1.0, 그 외 0.7
	 * - 2~3개: 1~2시간 1.0, 2~3시간=> 0.7, 그 외 0.4
	 * - 4개 이상: ~1시간 1.0, 1.5~2시간 => 0.7, 그 외 0.4
	 */
	private double calculateStayScore(List<Vlock> usedVlocksInTemplate, Vlock candidate) {
		int usedCount = usedVlocksInTemplate.size();
		double stayHour = candidate.getVlockCategory().getStayHours();

		if (usedCount <= 1) {
			if (stayHour >= 1 && stayHour <= 3)
				return 1.0;
			return 0.7;
		}
		if (usedCount <= 3) {
			if (stayHour >= 1 && stayHour <= 2)
				return 1.0;
			if (stayHour <= 3)
				return 0.7;
			return 0.4;
		}
		if (stayHour <= 1.5)
			return 1.0;
		if (stayHour <= 2)
			return 0.7;
		return 0.4;
	}

	/**
	 * 이동시간 점수를 계산하는 메서드
	 * - 거리(km)를 이동수단 별 평균속도를 이용해 이동시간 계산
	 *
	 * 이동 수단 별 평균 속도
	 * - WALK: 4km/h
	 * - TRANSIT: 20km/h
	 * - CAR: 30km/h
	 *
	 * 점수
	 * - 0~10분 -> 1.0
	 * - 10~20분 -> 0.8
	 * - 20~35 -> 0.5
	 * - 35~ -> 0.2
	 */
	private double travelTimeScoreFromDistance(double km, TransportType type) {
		double kmPerHour = switch (type) {
			case WALK -> 4.0;
			case TRANSIT -> 20.0;
			case CAR -> 30.0;
		};
		double minutes = (km / kmPerHour) * 60.0;

		if (minutes <= 15)
			return 1.0;
		if (minutes <= 25)
			return 0.8;
		if (minutes <= 35)
			return 0.5;
		if (minutes <= 45)
			return 0.3;
		return 0.1;
	}

	/**
	 *  랜덤 3개의 추천 Vlock 선택 및 반복 방지를 위해 recent를 갱신하는 메서드
	 */
	private List<VlockSuggestionsResponseDTO.VlockSuggestionCardDTO> pick3FromPoolAndUpdateRecent(Long templateId,
		CachedVlockSuggestions cached, long seed) {
		Random random = new Random(seed);

		List<Long> vlockIds = new ArrayList<>(cached.vlockIds());
		Collections.shuffle(vlockIds, random);

		List<Long> recent = cached.recentPickedIds() == null ? new ArrayList<>()
			: new ArrayList<>(cached.recentPickedIds());
		Set<Long> ban = new HashSet<>(recent);

		List<Long> pickedVlockIds = new ArrayList<>(vlockSuggestionSize);

		for (Long vlockId : vlockIds) {
			if (pickedVlockIds.size() == vlockSuggestionSize)
				break;
			if (ban.contains(vlockId))
				continue;
			pickedVlockIds.add(vlockId);
		}

		// 수가 적어 recent를 제외할 경우 추천 개수가 채워지지 않으면 recent여도 이용
		if (pickedVlockIds.size() < vlockSuggestionSize) {
			for (Long id : vlockIds) {
				if (pickedVlockIds.size() == vlockSuggestionSize)
					break;
				if (pickedVlockIds.contains(id))
					continue;
				pickedVlockIds.add(id);
			}
		}

		// recent 갱신
		recent.clear();
		recent.addAll(pickedVlockIds);
		vlockSuggestionCache.set(templateId, cached.withRecentPickedIds(recent));

		List<Vlock> vlocks = vlockQueryService.getAllWithCategoryByVlockIds(pickedVlockIds);
		Map<Long, Vlock> map = vlocks.stream().collect(Collectors.toMap(Vlock::getId, v -> v));

		List<VlockSuggestionsResponseDTO.VlockSuggestionCardDTO> cards = new ArrayList<>(vlockSuggestionSize);
		for (Long vlockId : pickedVlockIds) {
			Vlock v = map.get(vlockId);
			if (v == null)
				continue;
			cards.add(VlockSuggestionsResponseDTO.VlockSuggestionCardDTO.from(v, s3Properties.domain()));
		}
		return cards;
	}

	/**
	 * 이동수단에 따른 이동거리(반지름)을 결정하는 메서드
	 */
	private double radiusKm(TransportType type) {
		return switch (type) {
			case WALK -> 10.0;
			case TRANSIT -> 15.0;
			case CAR -> 20.0;
		};
	}

	/**
	 * Day Vlock들의 중심으로부터 인근 Vlock들을 탐색하는 메서드
	 */
	private List<Vlock> findNearVlocksByCenter(
		List<Long> cityIds, Set<Long> exVlockIds, LatLng center, double radiusKm) {
		List<Long> excludeVlockIds = exVlockIds.isEmpty() ? List.of(-1L) : new ArrayList<>(exVlockIds);
		BoundingBox box = GeoUtil.box(center, radiusKm);
		List<Vlock> vlockCandidatesInBox = vlockQueryService.getVlocksInBoxExcluding(
			cityIds,
			excludeVlockIds,
			box.minLat(), box.maxLat(), box.minLng(), box.maxLng(),
			PageRequest.of(0, boxPool));

		// 위에선 위/경도 범위로 후보를 탐색하고, 아래에서 haversine으로 실제 radius 내인지 필터링
		return vlockCandidatesInBox.stream()
			.filter(v -> GeoUtil.haversineKm(center, new LatLng(v.getLatitude(), v.getLongitude())) <= radiusKm)
			.toList();
	}

	/**
	 * 추천 후보들을 뽑는 메서드
	 * - 해당 template에 블록이 없는 경우 Vlock 사용수로, 있는 경우 거리 및 Vlock 사용수로 선별
	 */
	private List<Vlock> pickAiCandidates(List<Vlock> filtered, List<Vlock> usedVlocksInTemplate, LatLng center) {
		if (filtered.size() <= aiCandidatePool) {
			return filtered;
		}

		// 해당 template에 블록이 없는 경우
		if (usedVlocksInTemplate.isEmpty() || center == null) {
			boolean allZero = filtered.stream()
				.allMatch(v -> Optional.ofNullable(v.getUsageCount()).orElse(0) == 0);

			if (allZero) {
				List<Vlock> copy = new ArrayList<>(filtered);
				Collections.shuffle(copy);
				return copy.stream().limit(aiCandidatePool).toList();
			}

			return filtered.stream()
				.sorted(
					Comparator.comparingInt((Vlock v) -> Optional.ofNullable(v.getUsageCount()).orElse(0)).reversed())
				.limit(aiCandidatePool)
				.toList();
		}

		return filtered.stream()
			.sorted(
				Comparator
					.comparingDouble(
						(Vlock v) -> GeoUtil.haversineKm(center, new LatLng(v.getLatitude(), v.getLongitude())))
					.thenComparing((Vlock v) -> Optional.ofNullable(v.getUsageCount()).orElse(0),
						Comparator.reverseOrder()))
			.limit(aiCandidatePool)
			.toList();
	}
}
