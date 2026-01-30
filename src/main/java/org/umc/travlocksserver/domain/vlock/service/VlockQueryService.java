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
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockResponseDTO;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VlockQueryService {

	private final MemberRepository memberRepository;
	private final CityRepository cityRepository;
	private final VlockRepository vlockRepository;

	/** 생성 블록 조회 */
	@Transactional(readOnly = true)
	public List<VlockResponseDTO> getMyVlock(Long memberId, Long cityId) {
		validateMemberExists(memberId);
		validateCityExists(cityId);

		return vlockRepository.findAllByOwnerIdAndCityIdAndDeletedAtIsNull(memberId, cityId)
			.stream()
			.map(VlockResponseDTO::from)
			.toList();
	}

	/** 인기 블록 조회 */
	@Transactional(readOnly = true)
	public List<VlockResponseDTO> getPopularVlocks(Long cityId) {
		validateCityExists(cityId);

		return vlockRepository.findAllByCityIdOrderByUsageCountDesc(cityId);
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
}
