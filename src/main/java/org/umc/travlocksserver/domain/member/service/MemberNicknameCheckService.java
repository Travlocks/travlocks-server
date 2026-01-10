package org.umc.travlocksserver.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.member.dto.response.MemberNicknameExistsResponseDTO;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberNicknameCheckService {

    private final MemberRepository memberRepository;

    public MemberNicknameExistsResponseDTO checkNicknameExists(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);

        // exists == true → 이미 사용 중 → available = false
        return new MemberNicknameExistsResponseDTO(!exists);
    }
}