
package org.umc.travlocksserver.domain.vlock.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.constant.CityErrorCode;
import org.umc.travlocksserver.domain.location.exception.CityException;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.vlock.code.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockResponseDTO;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VlockQueryService {

	private final MemberRepository memberRepository;
	private final CityRepository cityRepository;
	private final VlockCategoryRepository categoryRepository;
	private final VlockRepository vlockRepository;

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
			.map(VlockResponseDTO::from)
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
			throw new VlockException(VlockErrorCode.CATEGORY_NOT_FOUND);
		}
	}
}