package org.umc.travlocksserver.domain.vlock.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.location.service.query.CityQueryService;
import org.umc.travlocksserver.domain.template.projection.CityProjectionDTO;
import org.umc.travlocksserver.domain.template.service.query.TemplateCityQueryService;
import org.umc.travlocksserver.domain.vlock.code.VlockCategoryErrorCode;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.exception.VlockCategoryException;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.domain.vlock.service.query.VlockCategoryQueryService;
import org.umc.travlocksserver.global.geo.LatLng;
import org.umc.travlocksserver.infra.kakao.KakaoPlace;
import org.umc.travlocksserver.infra.kakao.KakaoPlaceClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VlockExternalCommandService {

    @Value("${kakao.keyword-search.size}")
    private int kakaoKeywordSearchSize;

    private final VlockRepository vlockRepository;
    private final TemplateCityQueryService templateCityQueryService;
    private final VlockCommandService vlockCommandService;
    private final KakaoPlaceClient kakaoPlaceClient;
    private final CityQueryService cityQueryService;
    private final VlockCategoryQueryService vlockCategoryQueryService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveVlocksFromExternal(KakaoPlace place, VlockCategory category, City city) {
        try {
            Vlock vlock = Vlock.createByExternal(
                    place.placeId(),
                    category,
                    city,
                    place.name(),
                    place.latitude(),
                    place.longitude(),
                    place.address()
            );

            vlockRepository.save(vlock);
        } catch(
                DataIntegrityViolationException e) {
            // UNIQUE 충돌 -> 이미 존재하는 블록 -> 무시
        }
    }

    /**
     * 카카오맵 API로부터 Vlock들을 추가하는 메서드
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void fetchFromExternal(Long templateId, LatLng center, Integer radiusKm) {
        List<CityProjectionDTO> cities = templateCityQueryService.getCitiesByTemplateId(templateId);
        if (cities.isEmpty())
            return;

        Double x = (center == null) ? null : center.lng();
        Double y = (center == null) ? null : center.lat();
        Integer radiusM = (center == null) ? null : radiusKm * 1000;

        for (CityProjectionDTO city : cities) {
            List<KakaoPlace> results = new ArrayList<>();
//            results.addAll(fetchKakaoPlaces(city.cityName() + " 관광지", x, y, radiusM));
//            results.addAll(fetchKakaoPlaces(city.cityName() + " 맛집", x, y, radiusM));
//            results.addAll(fetchKakaoPlaces(city.cityName() + " 카페", x, y, radiusM));
            results.addAll(fetchKakaoPlaces(city.cityName() + " 숙소", x, y, radiusM));

            List<KakaoPlace> deduplicated = deduplicateByPlaceId(results);

            upsertVlocksFromExternal(city.cityId(), deduplicated);
        }
    }

    /**
     * KakaoPlaceId 기준으로 중복되는 결과를 삭제하는 메서드
     */
    private List<KakaoPlace> deduplicateByPlaceId(List<KakaoPlace> places) {
        Map<String, KakaoPlace> result = new LinkedHashMap<>();
        for (KakaoPlace place : places) {
            if (place.placeId() == null)
                continue;
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


    // ⚪ 외부(카카오맵) API를 통해 블록을 삽입하는 메서드 (추천시 블록에 데이터가 너무 적을 경우 사용)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void upsertVlocksFromExternal(Long cityId, List<KakaoPlace> places) {
        City city = cityQueryService.getReferenceById(cityId);

        for (KakaoPlace place : places) {
            if (place.placeId() == null || place.placeId().isBlank())
                continue;

            VlockCategory category = mapCategory(place.categoryName());
            saveVlocksFromExternal(place, category, city);

//			boolean exists = vlockRepository.existsByExternalPlaceIdAndIsPublicTrue(place.placeId());
//
//			if (exists) {
//				continue;
//			}
//
//			try {
//				VlockCategory category = mapCategory(place.categoryName());
//
//				Vlock vlock = Vlock.createByExternal(
//					place.placeId(),
//					category,
//					city,
//					place.name(),
//					place.latitude(),
//					place.longitude(),
//					place.address()
//				);
//
//				vlockRepository.save(vlock);
//			} catch(DataIntegrityViolationException e) {
//				// UNIQUE 충돌 -> 이미 존재하는 블록 -> 무시
//			}
        }
    }

    // ⚪ 외부(카카오맵) API를 통해 가져온 카테고리를 우리 서비스 내의 카테고리로 매핑하는 메서드
    private VlockCategory mapCategory(String name) {
        String mappedName = switch (name) {
            case "음식점" -> "식당";
            case "카페" -> "카페";
            case "관광명소" -> "관광지";
            case "문화시설" -> "문화";
            default -> "기타";
        };

        return vlockCategoryQueryService.getByName(mappedName)
                .orElseGet(() -> vlockCategoryQueryService.getByName("기타")
                        .orElseThrow(
                                () -> new VlockCategoryException(VlockCategoryErrorCode.DEFAULT_VLOCK_CATEGORY_NOT_FOUND)));
    }
}
