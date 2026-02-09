package org.umc.travlocksserver.domain.member.service.command;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.auth.service.command.AuthService;
import org.umc.travlocksserver.domain.auth.service.command.SignupTokenService;
import org.umc.travlocksserver.domain.member.dto.request.MemberSignupRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.enums.MemberStatus;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.*;
import org.umc.travlocksserver.global.profile.DefaultProfileImageProvider;

@Service
@RequiredArgsConstructor
public class MemberSignupService {
    private final MemberRepository memberRepository;
    private final SignupTokenService signupTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final DefaultProfileImageProvider defaultProfileImageProvider;
    private final MemberConsentPreferenceProcessor processor;

    @Transactional
    public MemberSignupResponseDTO signup(MemberSignupRequestDTO request, HttpServletResponse response) {

        // signupToken 검증 + 토큰에 저장된 email 조회
        String tokenEmail = signupTokenService.getEmail(request.signupToken());
        if (tokenEmail == null) {
            throw new MemberException(MemberErrorCode.SIGNUP_TOKEN_INVALID);
        }
        if (!tokenEmail.equalsIgnoreCase(request.email())) {
            throw new MemberException(MemberErrorCode.SIGNUP_TOKEN_EMAIL_MISMATCH);
        }

        // 이메일/닉네임 중복 체크
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new MemberException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 기본 프로필 이미지 랜덤 고정 배정
        String profileImageUrl = defaultProfileImageProvider.pickRandomUrl();

        // Member 생성
        Member member = Member.builder()
                .email(request.email())
                .nickname(request.nickname())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(MemberStatus.ACTIVE)
                .emailVerified(true)
                .profileImageUrl(profileImageUrl)
                .vlockCount(0)
                .templateCount(0)
                .starCount(0)
                .build();

        Member savedMember = memberRepository.save(member);

        // 약관/선호 처리
        var result = processor.process(
                savedMember,
                request.consents().stream()
                        .map(c -> new MemberConsentPreferenceProcessor.ConsentInput(c.policyId(), c.agreed()))
                        .toList(),
                request.preferredTravelStyleIds(),
                request.preferredTravelThemeIds(),
                false
        );

        // 회원가입 성공 시 signupToken 삭제
        signupTokenService.consume(request.signupToken());

        // AccessToken, RefreshToken 발급
        AuthService.IssuedTokens tokens = authService.issueTokens(savedMember.getId(), response);

        return new MemberSignupResponseDTO(
                savedMember.getId(),
                savedMember.getNickname(),
                tokens.accessToken(),
                tokens.accessTokenExpiresIn(),
                profileImageUrl,
                result.preferredThemes(),
                result.preferredStyles()
        );
    }

}

