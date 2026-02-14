package org.umc.travlocksserver.domain.vlock.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.constant.CityErrorCode;
import org.umc.travlocksserver.domain.location.exception.CityException;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.vlock.code.VlockCategoryErrorCode;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockResponseDTO;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;
import org.umc.travlocksserver.global.aws.S3Properties;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VlockQueryService {

	private final VlockRepository vlockRepository;
	private final MemberRepository memberRepository;
	private final CityRepository cityRepository;
	private final VlockCategoryRepository categoryRepository;
	private final S3Properties s3Properties;

	public List<Vlock> getPopularByCityIds(List<Long> cityIds, Pageable pageable) {
		return vlockRepository.findPopularByCityIds(cityIds, pageable);
	}

	public List<Vlock> getAllById(List<Long> ids) {
		return vlockRepository.findAllById(ids);
	}

	public List<Vlock> getVlocksInBoxExcluding(
		List<Long> cityIds,
		List<Long> excludeVlockIds,
		double minLat,
		double maxLat,
		double minLng,
		double maxLng,
		Pageable pageable) {
		return vlockRepository.findVlocksInBoxExcluding(cityIds, excludeVlockIds, minLat, maxLat, minLng, maxLng,
			pageable);
	}

	/** 인기 블록 조회 */
	public List<VlockResponseDTO> getPopularVlocks(Long cityId) {
		validateCityExists(cityId);

		return vlockRepository
			.findPopularVlocks(cityId);
	}

	/** 카테고리 블록 조회 */
	public List<VlockResponseDTO> getCategoriesVlocks(Long cityId, Long categoryId) {
		validateCityExists(cityId);
		validateCategoryExists(categoryId);

		return vlockRepository
			.findCategoryVlocks(cityId, categoryId);
	}

	/** 생성 블록 조회 */
	public List<VlockResponseDTO> getMyVlocks(Long memberId, Long cityId) {
		validateMemberExists(memberId);
		validateCityExists(cityId);

		return vlockRepository
			.findAllByOwnerIdAndCityIdAndDeletedAtIsNullOrderByUsageCountDescIdDesc(memberId, cityId)
			.stream()
			.map(vlock -> VlockResponseDTO.from(vlock, s3Properties.domain()))
			.toList();
	}

	/** 블록 검색 */
	public List<VlockResponseDTO> searchVlocks(String keyword, Pageable pageable) {
		return vlockRepository.searchByKeyword(keyword, pageable)
			.stream()
			.map(vlock -> VlockResponseDTO.from(vlock, s3Properties.domain()))
			.toList();
	}

	private void validateMemberExists(Long memberId) {
		if (!memberRepository.existsById(memberId)) {
			throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
		}
	}

	private void validateCityExists(Long cityId) {
		if (!cityRepository.existsById(cityId)) {
			throw new CityException(CityErrorCode.CITY_NOT_FOUND);
		}
	}

	private void validateCategoryExists(Long categoryId) {
		if (!categoryRepository.existsById(categoryId)) {
			throw new VlockException(VlockCategoryErrorCode.DEFAULT_VLOCK_CATEGORY_NOT_FOUND);
		}
	}
}
