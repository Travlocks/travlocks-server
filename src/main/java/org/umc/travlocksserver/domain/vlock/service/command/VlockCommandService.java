package org.umc.travlocksserver.domain.vlock.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.vlock.constant.VlockCategoryErrorCode;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.exception.VlockCategoryException;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.infra.kakao.KakaoPlace;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VlockCommandService {

    private final VlockRepository vlockRepository;
    private final CityRepository cityRepository;
    private final VlockCategoryRepository vlockCategoryRepository;

    // ⚪ 외부(카카오맵) API를 통해 블록을 삽입하는 메서드 (추천시 블록에 데이터가 너무 적을 경우 사용)
    public void upsertVlocksFromExternal(Long cityId, List<KakaoPlace> places) {
        City city = cityRepository.getReferenceById(cityId);

        for (KakaoPlace place : places) {
            if (place.placeId() == null || place.placeId().isBlank()) continue;
            VlockCategory category = mapCategory(place.categoryName());

            // 공개된 Vlock의 ExternalplaceId 기준 Vlock 조회 후 없으면 생성
            Vlock vlock = vlockRepository.findByExternalPlaceIdAndIsPublicTrue(place.placeId())
                    .orElseGet(() -> Vlock.createByExternal(
                            place.placeId(),
                            category,
                            city,
                            place.name(),
                            place.latitude(),
                            place.longitude(),
                            place.address(),
                            place.placeUrl()
                    ));

            vlockRepository.save(vlock);
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

        return vlockCategoryRepository.findByName(name)
                .orElseGet(() -> vlockCategoryRepository.findByName("기타")
                        .orElseThrow(() -> new VlockCategoryException(VlockCategoryErrorCode.DEFAULT_BLOCK_CATEGORY_NOT_FOUND)));
    };
}
