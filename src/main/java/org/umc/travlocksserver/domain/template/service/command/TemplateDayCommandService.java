package org.umc.travlocksserver.domain.template.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.dto.response.VlockSuggestionsResponseDTO;
import org.umc.travlocksserver.domain.template.entity.Template;
import org.umc.travlocksserver.domain.template.entity.TemplateDay;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.exception.TemplateDayException;
import org.umc.travlocksserver.domain.template.exception.code.TemplateDayErrorCode;
import org.umc.travlocksserver.domain.template.projection.CityProjectionDTO;
import org.umc.travlocksserver.domain.template.repository.TemplateCityRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateDayRepository;
import org.umc.travlocksserver.domain.template.repository.TemplateVlockRepository;
import org.umc.travlocksserver.domain.template.service.query.TemplateCityQueryService;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.domain.vlock.service.command.VlockCommandService;
import org.umc.travlocksserver.global.geo.BoundingBox;
import org.umc.travlocksserver.global.geo.GeoUtil;
import org.umc.travlocksserver.global.geo.LatLng;
import org.umc.travlocksserver.infra.ai.HyperClovaSuggestionClient;
import org.umc.travlocksserver.infra.ai.ScoredCandidate;
import org.umc.travlocksserver.infra.kakao.KakaoPlaceClient;
import org.umc.travlocksserver.infra.kakao.KakaoPlace;
import org.umc.travlocksserver.infra.redis.vlock.CachedVlockSuggestions;
import org.umc.travlocksserver.infra.redis.vlock.VlockSuggestionCache;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateDayCommandService {

    private final TemplateDayRepository templateDayRepository;
    private final TemplateVlockRepository templateVlockRepository;
    private final VlockRepository vlockRepository;
    private final TemplateCityRepository templateCityRepository;

    private final TemplateCityQueryService templateCityQueryService;
    private final VlockCommandService vlockCommandService;

    private final VlockSuggestionCache vlockSuggestionCache;

    private final KakaoPlaceClient kakaoPlaceClient;
    private final HyperClovaSuggestionClient aiClient;

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

    @Value("${kakao.keyword-search.size}")
    private int kakaoKeywordSearchSize;

    @Value("${suggestion.vlock.size}")
    private int vlockSuggestionSize;

    @Value("${suggestion.vlock.max-duplicate-category}")
    private int maxDuplicateCategory;

    /**
     * Vlock 추천 결과를 반환하는 메서드
     */
    public VlockSuggestionsResponseDTO suggestVlocks(Long memberId, Long templateDayId) {
        long seed = System.currentTimeMillis();

        // TemplateDay 존재 및 권한 검증
        TemplateDay templateDay = templateDayRepository.findByIdAndTemplateOwnerId(templateDayId, memberId)
                .orElseThrow(() -> new TemplateDayException(TemplateDayErrorCode.TEMPLATE_DAY_NOT_FOUND));

        // 캐시 hit
        CachedVlockSuggestions cached = vlockSuggestionCache.get(templateDayId);
        if (cached != null && cached.vlockIds() != null && cached.vlockIds().size() >= 3) {
            List<VlockSuggestionsResponseDTO.VlockSuggestionCardDTO> cards = pick3FromPoolAndUpdateRecent(templateDayId, cached, seed);
            return VlockSuggestionsResponseDTO.from(templateDayId, cards, seed, true);
        }

        // 캐시 miss
        CachedVlockSuggestions newCached = buildSuggestion(templateDay);
        if (newCached.vlockIds() == null || newCached.vlockIds().size() < 3) {
            return VlockSuggestionsResponseDTO.from(templateDayId, List.of(), seed, false);
        }

        vlockSuggestionCache.set(templateDayId, newCached);

        List<VlockSuggestionsResponseDTO.VlockSuggestionCardDTO> cards = pick3FromPoolAndUpdateRecent(templateDayId, newCached, seed);
        return VlockSuggestionsResponseDTO.from(templateDayId, cards, seed, false);
    }

    /**
     * Redis에 저장할 추천 블록을 만드는 메서드
     */
    private CachedVlockSuggestions buildSuggestion(TemplateDay templateDay) {
        Template template = templateDay.getTemplate();
        Long templateId = template.getId();
        TransportType transportType = template.getTransportType();
        List<Long> cityIdsOfTemplate = templateCityQueryService.getCityIdsByTemplateId(templateId);

        // 해당 템플릿에서 사용중인 블록 조회 (중복 제외)
        Set<Long> usedVlockIdsInTemplate = new HashSet<>(templateVlockRepository.findAllVlockIdsByTemplateDayTemplateId(templateId));

        // 해당 템플릿 데이에 사용중인 블록 조회
        List<Vlock> usedVlocksInDay = templateVlockRepository.findDistinctVlocksByTemplateDayId(templateDay.getId());

        // 추천 블록 후보
        List<Vlock> candidates;

        // 이동반경
        double radiusKm = radiusKm(transportType);

        // 현재 TemplateDay에 들어간 블록들의 중심 좌표
        LatLng center = usedVlocksInDay.isEmpty() ? null : LatLng.averageFrom(usedVlocksInDay);

        // 추천 블록 후보 조회 또는 추가
        if (usedVlocksInDay.isEmpty()) {
            // 템플릿에 해당 day 블록이 0개면 지역 인기 추천
            candidates = vlockRepository.findPopularByCityIds(cityIdsOfTemplate, PageRequest.of(0, popularPool));

            // 블록 추천 후보 수가 minPool 이하면 외부 API(카카오맵)에서 가져와서 저장
            if (candidates.size() < minPool) {
                fetchFromExternal(templateId, null, null);
                candidates = vlockRepository.findPopularByCityIds(cityIdsOfTemplate, PageRequest.of(0, popularPool));
            }

        } else {
            // 템플릿에 해당 day 블록이 존재하면 블록들 위치 기반 추천
            candidates = findNearVlocksByCenter(cityIdsOfTemplate, usedVlockIdsInTemplate, center, radiusKm);

            if (candidates.size() < minPool) {
                fetchFromExternal(templateId, center, (int) (radiusKm));
                candidates = findNearVlocksByCenter(cityIdsOfTemplate, usedVlockIdsInTemplate, center, radiusKm);
            }
        }

        // 템플릿 내에 이미 존재하는 장소나 숙소는 추천 후보에서 제거
        List<Vlock> filtered = candidates.stream()
                .filter(v -> !usedVlockIdsInTemplate.contains(v.getId()))
                .filter(v -> !"숙소".equals(v.getVlockCategory().getName()))
                .toList();

        // 후보 선정
        List<Vlock> aiCandidates = pickAiCandidates(filtered, usedVlocksInDay, center);

        // AI 적합도 점수
        Map<Long, Double> aiScores = aiClient.requestToAi(templateDay.getId(), usedVlocksInDay, aiCandidates);

        // TemplateDay에 들어간 vlock들의 카테고리 종류 목록
        Set<String> categoriesInDay = usedVlocksInDay.stream()
                .map(v -> v.getVlockCategory().getName())
                .collect(Collectors.toSet());

        List<ScoredCandidate> scored = new ArrayList<>(aiCandidates.size());

        for (Vlock v : aiCandidates) {
            // AI 적합도
            double aiScore = aiScores.getOrDefault(v.getId(), 0.5);

            // 이동 시간 적합도
            double moveScore = (center == null)
                    ? 1.0
                    : travelTimeScoreFromDistance(
                    GeoUtil.haversineKm(center, new LatLng(v.getLatitude(), v.getLongitude())),
                    transportType
            );

            // 체류 적합도
            double stayScore = calculateStayScore(usedVlocksInDay, v);

            // 장소 신뢰도
            double reliableScore = calculateReliableScore(v.getUsageCount());

            // 다양성 보정
            double diversityScore = calculateDiversityScore(categoriesInDay, v.getVlockCategory().getName());

            double finalScore =
                    aiScore * 0.35 + moveScore * 0.30 + stayScore * 0.20 + reliableScore + 0.10 + diversityScore * 0.05;

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
            if (picked.size() >= poolSize) break;

            int c = categoryCount.getOrDefault(sc.vlock().getVlockCategory().getName(), 0);
            if (c >= maxDuplicateCategory) continue;

            picked.add(sc.vlock().getId());
            categoryCount.put(sc.vlock().getVlockCategory().getName(), c + 1);
        }

        if (picked.size() < Math.min(poolSize, sorted.size())) {
            for (ScoredCandidate sc : sorted) {
                if (picked.size() >= poolSize) break;
                if (picked.contains(sc.vlock().getId())) continue;
                picked.add(sc.vlock().getId());
            }
        }

        return picked;
    }


    /**
     * 다양성 적합도 점수를 계산하는 메서드
     * 현재 day에 없는 카테고리면 1.0, 있으면 0.3
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
        if (usageCount <= 0) return 0.1;
        if (usageCount <= 3) return 0.4;
        if (usageCount <= 10) return 0.7;
        return 1.0;
    }

    /**
     * 체류 적합도 점수를 계산하는 메서드 (기존 블록수 및 후보 체류시간 기반)
     *
     * - 0~1개: 1~2시간 1.0, 그 외 0.7
     * - 2~3개: 1~2시간 1.0, 120~180 => 0.7, 그 외 0.4
     * - 4개 이상: ~1시간 1.0, 1.5~2시간 => 0.7, 그 외 0.4
     */
    private double calculateStayScore(List<Vlock> usedVlocksInDay, Vlock candidate) {
        int usedCount = usedVlocksInDay.size();
        double stayHour = candidate.getVlockCategory().getStayHours();

        if (usedCount <= 1) {
            if (stayHour >= 1 && stayHour <= 3) return 1.0;
            return 0.7;
        }
        if (usedCount <= 3) {
            if (stayHour >= 1 && stayHour <= 2) return 1.0;
            if (stayHour <= 3) return 0.7;
            return 0.4;
        }
        if (stayHour <= 1.5) return 1.0;
        if (stayHour <= 2) return 0.7;
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

        if (minutes <= 10) return 1.0;
        if (minutes <= 20) return 0.8;
        if (minutes <= 35) return 0.5;
        return 0.2;
    }

    /**
     *  랜덤 3개의 추천 Vlock 선택 및 반복 방지를 위해 recent를 갱신하는 메서드
     */
    private List<VlockSuggestionsResponseDTO.VlockSuggestionCardDTO> pick3FromPoolAndUpdateRecent(Long templateDayId, CachedVlockSuggestions cached, long seed) {
        Random random = new Random(seed);

        List<Long> vlockIds = new ArrayList<>(cached.vlockIds());
        Collections.shuffle(vlockIds, random);

        List<Long> recent = cached.recentPickedIds() == null ? new ArrayList<>() : new ArrayList<>(cached.recentPickedIds());
        Set<Long> ban = new HashSet<>(recent);

        List<Long> pickedVlockIds = new ArrayList<>(vlockSuggestionSize);

        for (Long vlockId : vlockIds) {
            if (pickedVlockIds.size() == vlockSuggestionSize) break;
            if (ban.contains(vlockId)) continue;
            pickedVlockIds.add(vlockId);
        }

        // 수가 적어 recent를 제외할 경우 추천 개수가 채워지지 않으면 recent여도 이용
        if (pickedVlockIds.size() < vlockSuggestionSize) {
            for (Long id : vlockIds) {
                if (pickedVlockIds.size() == vlockSuggestionSize) break;
                if (pickedVlockIds.contains(id)) continue;
                pickedVlockIds.add(id);
            }
        }

        // recent 갱신
        recent.clear();
        recent.addAll(pickedVlockIds);
        vlockSuggestionCache.set(templateDayId, cached.withRecentPickedIds(recent));

        List<Vlock> vlocks = vlockRepository.findAllById(pickedVlockIds);
        Map<Long, Vlock> map = vlocks.stream().collect(Collectors.toMap(Vlock::getId, v -> v));

        List<VlockSuggestionsResponseDTO.VlockSuggestionCardDTO> cards = new ArrayList<>(vlockSuggestionSize);
        for (Long vlockId : pickedVlockIds) {
            Vlock v = map.get(vlockId);
            if (v == null) continue;
            cards.add(VlockSuggestionsResponseDTO.VlockSuggestionCardDTO.from(v));
        }
        return cards;
    }

    /**
     * 이동수단에 따른 이동거리(반지름)을 결정하는 메서드
     */
    private double radiusKm(TransportType type) {
        return switch (type) {
            case WALK -> 2.0;
            case TRANSIT -> 5.0;
            case CAR -> 10.0;
        };
    }

    /**
     * Day Vlock들의 중심으로부터 인근 Vlock들을 탐색하는 메서드
     */
    private List<Vlock> findNearVlocksByCenter(
            List<Long> cityIds,  Set<Long> exVlockIds, LatLng center, double radiusKm
    ) {
        List<Long> excludeVlockIds = exVlockIds.isEmpty() ? List.of(-1L) : new ArrayList<>(exVlockIds);
        BoundingBox box = GeoUtil.box(center, radiusKm);
        List<Vlock> vlockCandidatesInBox = vlockRepository.findVlocksInBoxExcluding(
                cityIds,
                excludeVlockIds,
                box.minLat(), box.maxLat(), box.minLng(), box.maxLng(),
                PageRequest.of(0, boxPool)
        );

        // 위에선 위/경도 범위로 후보를 탐색하고, 아래에서 haversine으로 실제 radius 내인지 필터링
        return vlockCandidatesInBox.stream()
                .filter(v -> GeoUtil.haversineKm(center, new LatLng(v.getLatitude(), v.getLongitude())) <= radiusKm)
                .toList();
    }

    /**
     * 카카오맵 API로부터 Vlock들을 추가하는 메서드
     */
    private void fetchFromExternal(Long templateId, LatLng center, Integer radiusKm) {
        List<CityProjectionDTO> cities = templateCityRepository.findCitiesByTemplateId(templateId);
        if (cities.isEmpty()) return;

        Double x = (center == null) ? null : center.lng();
        Double y = (center == null) ? null : center.lat();
        Integer radiusM = (center == null) ? null : radiusKm * 1000;

        for (CityProjectionDTO city : cities) {
            List<KakaoPlace> results = new ArrayList<>();
            results.addAll(fetchKakaoPlaces(city.cityName() + "관광지", x, y, radiusM));
            results.addAll(fetchKakaoPlaces(city.cityName() + "맛집", x, y, radiusM));
            results.addAll(fetchKakaoPlaces(city.cityName() + "카페", x, y, radiusM));
            List<KakaoPlace> deduplicated = deduplicateByPlaceId(results);
            vlockCommandService.upsertVlocksFromExternal(city.cityId(), deduplicated);
        }
    }

    /**
     * KakaoPlaceId 기준으로 중복되는 결과를 삭제하는 메서드
     */
    private List<KakaoPlace> deduplicateByPlaceId(List<KakaoPlace> places) {
        Map<String, KakaoPlace> result = new LinkedHashMap<>();
        for (KakaoPlace place : places) {
            if (place.placeId() == null) continue;
            result.put(place.placeId(), place);
        }
        return new ArrayList<>(result.values());
    }

    /**
     * 외부(카카오맵) API를 통해 키워드 검색 후 내부 KakaoPlaceDTO로 변환하는 메서드
     */
    private List<KakaoPlace> fetchKakaoPlaces(String query, Double lng, Double lat, Integer radiusM) {
        return kakaoPlaceClient.searchPlaces(query, lng, lat, radiusM, kakaoKeywordSearchSize);
    }

    /**
     * 추천 후보들을 뽑는 메서드
     * - 해당 templateDay에 블록이 없는 경우 Vlock 사용수로, 있는 경우 거리 및 Vlock 사용수로 선별
     */
    private List<Vlock> pickAiCandidates(List<Vlock> filtered, List<Vlock> usedVlocksInDay, LatLng center) {
        if (filtered.size() <= aiCandidatePool) {
            return filtered;
        }

        // 해당 day에 블록이 없는 경우
        if (usedVlocksInDay.isEmpty() || center == null) {
            boolean allZero = filtered.stream()
                    .allMatch(v -> Optional.ofNullable(v.getUsageCount()).orElse(0) == 0);

            if (allZero) {
                List<Vlock> copy = new ArrayList<>(filtered);
                Collections.shuffle(copy);
                return copy.stream().limit(aiCandidatePool).toList();
            }

            return filtered.stream()
                    .sorted(Comparator.comparingInt((Vlock v) -> Optional.ofNullable(v.getUsageCount()).orElse(0)).reversed())
                    .limit(aiCandidatePool)
                    .toList();
        }

        return filtered.stream()
                .sorted(
                        Comparator.comparingDouble((Vlock v) ->
                                        GeoUtil.haversineKm(center, new LatLng(v.getLatitude(), v.getLongitude())))
                                .thenComparing((Vlock v) -> Optional.ofNullable(v.getUsageCount()).orElse(0), Comparator.reverseOrder())
                )
                .limit(aiCandidatePool)
                .toList();
    }
}
