package org.umc.travlocksserver.domain.vlock.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.constant.CityErrorCode;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.location.exception.CityException;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.location.service.query.CityQueryService;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.vlock.constant.VlockCategoryErrorCode;
import org.umc.travlocksserver.domain.vlock.constant.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.exception.VlockCategoryException;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.domain.vlock.service.VlockAsyncHandler;
import org.umc.travlocksserver.domain.vlock.service.query.VlockCategoryQueryService;
import org.umc.travlocksserver.infra.kakao.KakaoPlace;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VlockCommandService {

    private final VlockRepository vlockRepository;
    private final CityQueryService cityQueryService;
    private final VlockCategoryQueryService vlockCategoryQueryService;
    private final VlockCategoryRepository vlockCategoryRepository;
    private final CityRepository cityRepository;
    private final MemberRepository memberRepository;
    private final VlockAsyncHandler vlockAsyncHandler;

    // ⚪ 외부(카카오맵) API를 통해 블록을 삽입하는 메서드 (추천시 블록에 데이터가 너무 적을 경우 사용)
    public void upsertVlocksFromExternal(Long cityId, List<KakaoPlace> places) {
        City city = cityQueryService.getReferenceById(cityId);

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

        return vlockCategoryQueryService.getByName(name)
                .orElseGet(() -> vlockCategoryQueryService.getByName("기타")
                        .orElseThrow(() -> new VlockCategoryException(VlockCategoryErrorCode.DEFAULT_VLOCK_CATEGORY_NOT_FOUND)));
    };

    public void createVlock(Long memberId, VlockRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        VlockCategory category = vlockCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new VlockException(VlockErrorCode.CATEGORY_NOT_FOUND));

        City city = cityRepository.findWithRegionById(request.cityId())
                .orElseThrow(() -> new CityException(CityErrorCode.CITY_NOT_FOUND));

        vlockAsyncHandler.saveVlockAsync(member, category, city, request);
    }
}
