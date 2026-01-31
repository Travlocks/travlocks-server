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
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.exception.VlockException;
import org.umc.travlocksserver.domain.vlock.constant.VlockErrorCode;
import org.umc.travlocksserver.domain.vlock.repository.VlockCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class VlockCommandService {

	private final VlockCategoryRepository vlockCategoryRepository;
	private final CityRepository cityRepository;
	private final MemberRepository memberRepository;
	private final VlockAsyncHandler vlockAsyncHandler;

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

