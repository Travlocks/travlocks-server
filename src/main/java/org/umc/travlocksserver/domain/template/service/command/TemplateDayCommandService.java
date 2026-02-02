package org.umc.travlocksserver.domain.template.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.template.enums.TransportType;
import org.umc.travlocksserver.domain.template.projection.CityProjectionDTO;
import org.umc.travlocksserver.domain.template.repository.TemplateCityRepository;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.domain.vlock.service.command.VlockCommandService;
import org.umc.travlocksserver.global.geo.BoundingBox;
import org.umc.travlocksserver.global.geo.GeoUtil;
import org.umc.travlocksserver.global.geo.LatLng;
import org.umc.travlocksserver.infra.kakao.KakaoPlace;
import org.umc.travlocksserver.infra.kakao.KakaoPlaceClient;

import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional
public class TemplateDayCommandService {

    private final VlockRepository vlockRepository;
    private final TemplateCityRepository templateCityRepository;

    private final VlockCommandService vlockCommandService;

    private final KakaoPlaceClient kakaoPlaceClient;

    @Value("${suggestion.vlock.box-pool}")
    private int boxPool;

    @Value("${kakao.keyword-search.size}")
    private int kakaoKeywordSearchSize;


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
            List<Long> cityIds, Set<Long> exVlockIds, LatLng center, double radiusKm
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
}
