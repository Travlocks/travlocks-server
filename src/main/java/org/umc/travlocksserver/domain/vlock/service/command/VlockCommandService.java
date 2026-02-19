package org.umc.travlocksserver.domain.vlock.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.umc.travlocksserver.domain.location.constant.CityErrorCode;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.location.exception.CityException;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.location.service.query.CityQueryService;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.vlock.code.VlockCategoryErrorCode;
import org.umc.travlocksserver.domain.vlock.code.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.dto.command.VlockCreateCommand;
import org.umc.travlocksserver.domain.vlock.dto.command.VlockUpdateCommand;
import org.umc.travlocksserver.domain.vlock.dto.request.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.request.VlockUpdateRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockResponseDTO;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.exception.VlockCategoryException;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.domain.vlock.service.query.VlockCategoryQueryService;
import org.umc.travlocksserver.global.aws.S3Properties;
import org.umc.travlocksserver.global.aws.S3Provider;
import org.umc.travlocksserver.infra.kakao.KakaoPlace;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VlockCommandService {

	private final MemberRepository memberRepository;
	private final VlockCategoryRepository vlockCategoryRepository;
	private final CityRepository cityRepository;
	private final VlockRepository vlockRepository;
	private final VlockCategoryQueryService vlockCategoryQueryService;
	private final CityQueryService cityQueryService;
	private final S3Provider s3Provider;
	private final S3Properties s3Properties;
	private final VlockSaveCommandService vlockSaveCommandService;

	public VlockResponseDTO createVlock(Long memberId, VlockRequestDTO request, MultipartFile coverImg) {
		String imageUrl = uploadImageIfPresent(coverImg);

		VlockCreateCommand command = new VlockCreateCommand(
			getCategory(request.categoryId()),
			getCity(request.cityId()),
			getMember(memberId),
			request.name(), request.address(), request.memo(),
			imageUrl, request.latitude(), request.longitude());

		Vlock savedVlock = vlockRepository.save(Vlock.create(command));

		return VlockResponseDTO.from(savedVlock, s3Properties.domain());
	}

	public VlockResponseDTO updateVlock(
		Long memberId,
		Long vlockId,
		VlockUpdateRequestDTO request,
		MultipartFile coverImg) {
		validateMemberExists(memberId);
		Vlock vlock = getOwnedVlock(memberId, vlockId);

		String oldImageUrl = vlock.getCoverImgUrl();
		String newImageUrl = determineNewImageUrl(request, coverImg, oldImageUrl);

		VlockUpdateCommand command = new VlockUpdateCommand(
			getCategory(request.categoryId()),
			getCity(request.cityId()),
			request.name(), request.latitude(), request.longitude(),
			request.address(), request.memo(),
			newImageUrl, request.isPublic());

		vlock.update(command);

		if (shouldDeleteOldFile(request, coverImg, oldImageUrl)) {
			s3Provider.deleteFile(oldImageUrl);
		}

		return VlockResponseDTO.from(vlock, s3Properties.domain());
	}

	public void deleteVlock(Long memberId, Long vlockId) {
		Vlock vlock = vlockRepository.findById(vlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.VLOCK_NOT_FOUND));

		if (vlock.isDeleted()) {
			throw new VlockException(VlockErrorCode.VLOCK_ALREADY_DELETED);
		}

		if (!vlock.isOwnedBy(memberId)) {
			throw new VlockException(VlockErrorCode.VLOCK_FORBIDDEN);
		}

		vlock.softDelete();
	}

	// ⚪ 외부(카카오맵) API를 통해 블록을 삽입하는 메서드 (추천시 블록에 데이터가 너무 적을 경우 사용)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void upsertVlocksFromExternal(Long cityId, List<KakaoPlace> places) {
		City city = cityQueryService.getReferenceById(cityId);

		for (KakaoPlace place : places) {
			if (place.placeId() == null || place.placeId().isBlank())
				continue;

			VlockCategory category = mapCategory(place.categoryName());
			vlockSaveCommandService.saveVlocksFromExternal(place, category, city);

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

	private void validateMemberExists(Long memberId) {
		if (!memberRepository.existsById(memberId)) {
			throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
		}
	}

	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
	}

	private VlockCategory getCategory(Long categoryId) {
		return vlockCategoryRepository.findById(categoryId)
			.orElseThrow(() -> new VlockException(VlockCategoryErrorCode.DEFAULT_VLOCK_CATEGORY_NOT_FOUND));
	}

	private City getCity(Long cityId) {
		return cityRepository.findWithRegionById(cityId)
			.orElseThrow(() -> new CityException(CityErrorCode.CITY_NOT_FOUND));
	}

	private Vlock getOwnedVlock(Long memberId, Long vlockId) {
		Vlock vlock = vlockRepository.findByIdAndDeletedAtIsNull(vlockId)
			.orElseThrow(() -> new VlockException(VlockErrorCode.VLOCK_NOT_FOUND));

		if (!vlock.isOwnedBy(memberId)) {
			throw new VlockException(VlockErrorCode.VLOCK_FORBIDDEN);
		}
		return vlock;
	}

	private String uploadImageIfPresent(MultipartFile coverImg) {
		if (coverImg == null || coverImg.isEmpty()) {
			return null;
		}
		return s3Provider.uploadVlockFile(coverImg);
	}

	private String determineNewImageUrl(VlockUpdateRequestDTO request, MultipartFile coverImg, String oldImageUrl) {
		if (coverImg != null && !coverImg.isEmpty()) {
			return s3Provider.uploadVlockFile(coverImg);
		}

		if (Boolean.TRUE.equals(request.deleteCoverImg())) {
			return null;
		}

		return oldImageUrl;
	}

	private boolean shouldDeleteOldFile(VlockUpdateRequestDTO request, MultipartFile coverImg, String oldImageUrl) {
		if (oldImageUrl == null) {
			return false;
		}

		return (coverImg != null && !coverImg.isEmpty()) || Boolean.TRUE.equals(request.deleteCoverImg());
	}
}
