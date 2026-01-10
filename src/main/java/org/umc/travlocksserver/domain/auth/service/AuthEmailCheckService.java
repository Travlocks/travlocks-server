package org.umc.travlocksserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.travlocksserver.domain.auth.dto.AuthEmailExistsResponseDTO;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthEmailCheckService {

    private final MemberRepository memberRepository;

    public AuthEmailExistsResponseDTO checkEmailExists(String email) {
        boolean exists = memberRepository.existsByEmail(email);
        return new AuthEmailExistsResponseDTO(exists);
    }
}

