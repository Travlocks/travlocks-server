package org.umc.travlocksserver.domain.vlock.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.location.entity.City;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.vlock.dto.vlock.VlockRequestDTO;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;
import org.umc.travlocksserver.domain.vlock.entity.VlockCategory;
import org.umc.travlocksserver.domain.vlock.repository.VlockRepository;

import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class VlockAsyncHandler {

	private final VlockRepository vlockRepository;

	@Async
	public void saveVlockAsync(Member member, VlockCategory category, City city, VlockRequestDTO request) {
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

		vlockRepository.save(newVlock);
	}
}
