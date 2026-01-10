package org.umc.travlocksserver.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.member.dto.response.MemberEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberEmailCheckService {

    private final MemberRepository memberRepository;

    public MemberEmailExistsResponseDTO checkEmailExists(String email) {
        boolean exists = memberRepository.existsByEmail(email);
        return new MemberEmailExistsResponseDTO(exists);
    }
}

