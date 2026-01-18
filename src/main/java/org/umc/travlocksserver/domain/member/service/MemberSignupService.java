package org.umc.travlocksserver.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.auth.service.SignupTokenService;
import org.umc.travlocksserver.domain.member.dto.request.MemberSignupRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.entity.Policy;
import org.umc.travlocksserver.domain.member.entity.TravelStyle;
import org.umc.travlocksserver.domain.member.entity.TravelTheme;
import org.umc.travlocksserver.domain.member.entity.mapping.MemberConsent;
import org.umc.travlocksserver.domain.member.entity.mapping.PreferredTravelStyle;
import org.umc.travlocksserver.domain.member.entity.mapping.PreferredTravelTheme;
import org.umc.travlocksserver.domain.member.enums.ConsentStatus;
import org.umc.travlocksserver.domain.member.enums.MemberStatus;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.exception.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberSignupService {

    private final MemberRepository memberRepository;
    private final PolicyRepository policyRepository;
    private final MemberConsentRepository memberConsentRepository;

    private final TravelStyleRepository travelStyleRepository;
    private final TravelThemeRepository travelThemeRepository;
    private final PreferredTravelStyleRepository preferredTravelStyleRepository;
    private final PreferredTravelThemeRepository preferredTravelThemeRepository;

    private final SignupTokenService signupTokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberSignupResponseDTO signup(MemberSignupRequestDTO request) {

        // 1) signupToken 검증 + 토큰에 저장된 email 조회(1회성이라면 여기서 소모)
        String tokenEmail = signupTokenService.consumeAndGetEmail(request.signupToken());
        if (tokenEmail == null) {
            throw new MemberException(MemberErrorCode.SIGNUP_TOKEN_INVALID);
        }
        if (!tokenEmail.equalsIgnoreCase(request.email())) {
            throw new MemberException(MemberErrorCode.SIGNUP_TOKEN_EMAIL_MISMATCH);
        }

        // 2) 이메일/닉네임 중복 체크
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new MemberException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 3) 약관 검증 (필수 약관은 AGREED 아니면 실패)
        List<Long> policyIds = request.consents().stream()
                .map(MemberSignupRequestDTO.ConsentDTO::policyId)
                .distinct()
                .toList();

        List<Policy> policies = policyRepository.findAllByIdIn(policyIds);
        if (policies.size() != policyIds.size()) {
            throw new MemberException(MemberErrorCode.POLICY_NOT_FOUND);
        }

        Map<Long, ConsentStatus> consentMap = request.consents().stream()
                .collect(Collectors.toMap(
                        MemberSignupRequestDTO.ConsentDTO::policyId,
                        MemberSignupRequestDTO.ConsentDTO::status,
                        (a, b) -> b
                ));

        for (Policy policy : policies) {
            if (policy.isRequired()) {
                ConsentStatus status = consentMap.get(policy.getId());
                if (status != ConsentStatus.AGREED) {
                    throw new MemberException(MemberErrorCode.REQUIRED_POLICY_NOT_AGREED);
                }
            }
        }

        // 4) Member 생성
        Member member = Member.builder()
                .email(request.email())
                .nickname(request.nickname())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(MemberStatus.ACTIVE)
                .emailVerified(true)
                .vlockCount(0)
                .templateCount(0)
                .starCount(0)
                .build();

        Member savedMember = memberRepository.save(member);

        // 5) member_consents 저장
        List<MemberConsent> memberConsents = policies.stream()
                .map(policy -> MemberConsent.builder()
                        .member(savedMember)
                        .policy(policy)
                        .status(consentMap.get(policy.getId()))
                        .build())
                .toList();

        memberConsentRepository.saveAll(memberConsents);

        // 6) 선호 스타일/테마 저장 (선택)
        if (request.preferredTravelStyleIds() != null && !request.preferredTravelStyleIds().isEmpty()) {
            savePreferredStyles(savedMember, request.preferredTravelStyleIds());
        }
        if (request.preferredTravelThemeIds() != null && !request.preferredTravelThemeIds().isEmpty()) {
            savePreferredThemes(savedMember, request.preferredTravelThemeIds());
        }

        return new MemberSignupResponseDTO(savedMember.getId(), savedMember.getNickname());
    }

    private void savePreferredStyles(Member member, List<Long> styleIds) {
        List<Long> distinct = styleIds.stream().distinct().toList();
        List<TravelStyle> styles = travelStyleRepository.findAllByIdIn(distinct);
        if (styles.size() != distinct.size()) {
            throw new MemberException(MemberErrorCode.TRAVEL_STYLE_NOT_FOUND);
        }

        List<PreferredTravelStyle> rows = styles.stream()
                .map(style -> PreferredTravelStyle.builder()
                        .member(member)
                        .travelStyle(style)
                        .build())
                .toList();

        preferredTravelStyleRepository.saveAll(rows);
    }

    private void savePreferredThemes(Member member, List<Long> themeIds) {
        List<Long> distinct = themeIds.stream().distinct().toList();
        List<TravelTheme> themes = travelThemeRepository.findAllByIdIn(distinct);
        if (themes.size() != distinct.size()) {
            throw new MemberException(MemberErrorCode.TRAVEL_THEME_NOT_FOUND);
        }

        List<PreferredTravelTheme> rows = themes.stream()
                .map(theme -> PreferredTravelTheme.builder()
                        .member(member)
                        .travelTheme(theme)
                        .build())
                .toList();

        preferredTravelThemeRepository.saveAll(rows);
    }
}

