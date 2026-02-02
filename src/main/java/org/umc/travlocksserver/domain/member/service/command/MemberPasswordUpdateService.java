package org.umc.travlocksserver.domain.member.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberPasswordUpdateService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updatePassword(Long memberId, String currentPassword, String newPassword) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!member.matchesPassword(passwordEncoder, currentPassword)) {
            throw new MemberException(MemberErrorCode.PASSWORD_MISMATCH);
        }

        if (member.matchesPassword(passwordEncoder, newPassword)) {
            throw new MemberException(MemberErrorCode.SAME_AS_CURRENT_PASSWORD);
        }

        member.changePassword(passwordEncoder.encode(newPassword));
    }
}
