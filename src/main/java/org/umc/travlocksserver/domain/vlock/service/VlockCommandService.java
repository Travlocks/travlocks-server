package org.umc.travlocksserver.domain.vlock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.constant.CityErrorCode;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.location.exception.CityException;
import org.umc.travlocksserver.domain.location.repository.CityRepository;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
import org.umc.travlocksserver.domain.vlock.dto.request.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.dto.response.VlockResponseDTO;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.code.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class VlockCommandService {

	private final VlockCategoryRepository vlockCategoryRepository;
	private final CityRepository cityRepository;
	private final MemberRepository memberRepository;
	private final VlockRepository vlockRepository;

	public VlockResponseDTO createVlock(Long memberId, VlockRequestDTO request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		VlockCategory category = vlockCategoryRepository.findById(request.categoryId())
			.orElseThrow(() -> new VlockException(VlockErrorCode.CATEGORY_NOT_FOUND));

		City city = cityRepository.findWithRegionById(request.cityId())
			.orElseThrow(() -> new CityException(CityErrorCode.CITY_NOT_FOUND));

		Vlock newVlock = Vlock.create(
			category,
			city,
			member,
			request.name(),
			request.latitude(),
			request.longitude(),
			request.address(),
			request.memo()
		);

		Vlock savedVlock = vlockRepository.save(newVlock);

		return VlockResponseDTO.from(savedVlock);
	}
}

