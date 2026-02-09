package org.umc.travlocksserver.domain.member.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.umc.travlocksserver.domain.member.dto.response.MemberSignupResponseDTO;
import org.umc.travlocksserver.domain.member.entity.MemberConsent;
import org.umc.travlocksserver.domain.member.entity.Policy;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberConsentRepository;
import org.umc.travlocksserver.domain.member.repository.PolicyRepository;
import org.umc.travlocksserver.domain.travelstyle.entity.PreferredTravelStyle;
import org.umc.travlocksserver.domain.travelstyle.entity.TravelStyle;
import org.umc.travlocksserver.domain.travelstyle.exception.TravelStyleException;
import org.umc.travlocksserver.domain.travelstyle.code.TravelStyleErrorCode;
import org.umc.travlocksserver.domain.travelstyle.repository.PreferredTravelStyleRepository;
import org.umc.travlocksserver.domain.travelstyle.repository.TravelStyleRepository;
import org.umc.travlocksserver.domain.traveltheme.entity.PreferredTravelTheme;
import org.umc.travlocksserver.domain.traveltheme.entity.TravelTheme;
import org.umc.travlocksserver.domain.traveltheme.exception.TravelThemeException;
import org.umc.travlocksserver.domain.traveltheme.code.TravelThemeErrorCode;
import org.umc.travlocksserver.domain.traveltheme.repository.PreferredTravelThemeRepository;
import org.umc.travlocksserver.domain.traveltheme.repository.TravelThemeRepository;
import org.umc.travlocksserver.domain.member.entity.Member;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemberConsentPreferenceProcessor {

    private static final int MAX_PREFERENCES = 2;

    private final PolicyRepository policyRepository;
    private final MemberConsentRepository memberConsentRepository;

    private final TravelStyleRepository travelStyleRepository;
    private final PreferredTravelStyleRepository preferredTravelStyleRepository;

    private final TravelThemeRepository travelThemeRepository;
    private final PreferredTravelThemeRepository preferredTravelThemeRepository;

    public record Result(
            List<MemberSignupResponseDTO.PreferredThemeItem> preferredThemes,
            List<MemberSignupResponseDTO.PreferredStyleItem> preferredStyles
    ) {}

    public record ConsentInput(Long policyId, Boolean agreed) {}

    /**
     * 공통 처리:
     * - 약관 정보/선호 정보 저장
     * - 응답용 PreferredThemeItem/PreferredStyleItem 리스트 생성/반환
     */
    public Result process(
            Member member,
            List<ConsentInput> consents,
            List<Long> preferredTravelStyleIds,
            List<Long> preferredTravelThemeIds,
            boolean replaceExisting
    ) {
        // 약관 검증 및 저장
        List<Policy> policies = validateAndGetPolicies(consents);
        Map<Long, Boolean> consentMap = consents.stream()
                .collect(Collectors.toMap(
                        ConsentInput::policyId,
                        c -> Boolean.TRUE.equals(c.agreed()),
                        (a, b) -> b
                ));

        if (replaceExisting) {
            memberConsentRepository.deleteByMemberId(member.getId());
        }

        List<MemberConsent> rows = policies.stream()
                .map(policy -> MemberConsent.builder()
                        .member(member)
                        .policy(policy)
                        .agreed(Boolean.TRUE.equals(consentMap.get(policy.getId())))
                        .build())
                .toList();

        memberConsentRepository.saveAll(rows);

        // 선호 검증 및 저장
        validateMax2(preferredTravelStyleIds, TravelStyleErrorCode.TRAVEL_STYLE_MAX_EXCEEDED);
        validateMax2(preferredTravelThemeIds, TravelThemeErrorCode.TRAVEL_THEME_MAX_EXCEEDED);

        if (replaceExisting) {
            preferredTravelStyleRepository.deleteByMemberId(member.getId());
            preferredTravelThemeRepository.deleteByMemberId(member.getId());
        }

        List<MemberSignupResponseDTO.PreferredStyleItem> preferredStyles = List.of();
        List<MemberSignupResponseDTO.PreferredThemeItem> preferredThemes = List.of();

        if (preferredTravelStyleIds != null && !preferredTravelStyleIds.isEmpty()) {
            List<TravelStyle> styles = savePreferredStyles(member, preferredTravelStyleIds);
            preferredStyles = styles.stream()
                    .map(s -> new MemberSignupResponseDTO.PreferredStyleItem(s.getId(), s.getContent()))
                    .toList();
        }

        if (preferredTravelThemeIds != null && !preferredTravelThemeIds.isEmpty()) {
            List<TravelTheme> themes = savePreferredThemes(member, preferredTravelThemeIds);
            preferredThemes = themes.stream()
                    .map(t -> new MemberSignupResponseDTO.PreferredThemeItem(t.getId(), t.getContent()))
                    .toList();
        }

        return new Result(preferredThemes, preferredStyles);
    }

    // ===== helpers =====
    private List<Policy> validateAndGetPolicies(List<ConsentInput> consents) {
        List<Long> policyIds = consents.stream()
                .map(ConsentInput::policyId)
                .distinct()
                .toList();

        List<Policy> policies = policyRepository.findAllByIdIn(policyIds);
        if (policies.size() != policyIds.size()) {
            throw new MemberException(MemberErrorCode.POLICY_NOT_FOUND);
        }

        Map<Long, Boolean> consentMap = consents.stream()
                .collect(Collectors.toMap(
                        ConsentInput::policyId,
                        c -> Boolean.TRUE.equals(c.agreed()),
                        (a, b) -> b
                ));

        for (Policy policy : policies) {
            if (policy.isRequired()) {
                Boolean agreed = consentMap.get(policy.getId());
                if (agreed == null || !agreed) {
                    throw new MemberException(MemberErrorCode.REQUIRED_POLICY_NOT_AGREED);
                }
            }
        }

        return policies;
    }

    private void validateMax2(List<Long> ids, Object errorCodeForException) {
        if (ids != null && ids.size() > MAX_PREFERENCES) {
            if (errorCodeForException instanceof TravelStyleErrorCode styleCode) {
                throw new TravelStyleException(styleCode);
            }
            if (errorCodeForException instanceof TravelThemeErrorCode themeCode) {
                throw new TravelThemeException(themeCode);
            }
        }
    }

    private List<TravelStyle> savePreferredStyles(Member member, List<Long> styleIds) {
        List<Long> distinct = styleIds.stream().distinct().toList();
        List<TravelStyle> styles = travelStyleRepository.findAllById(distinct); // 기본 메서드 사용
        if (styles.size() != distinct.size()) {
            throw new TravelStyleException(TravelStyleErrorCode.TRAVEL_STYLE_NOT_FOUND);
        }

        List<PreferredTravelStyle> rows = styles.stream()
                .map(style -> PreferredTravelStyle.builder()
                        .member(member)
                        .travelStyle(style)
                        .build())
                .toList();

        preferredTravelStyleRepository.saveAll(rows);
        return styles;
    }

    private List<TravelTheme> savePreferredThemes(Member member, List<Long> themeIds) {
        List<Long> distinct = themeIds.stream().distinct().toList();
        List<TravelTheme> themes = travelThemeRepository.findAllById(distinct); // 기본 메서드 사용
        if (themes.size() != distinct.size()) {
            throw new TravelThemeException(TravelThemeErrorCode.TRAVEL_THEME_NOT_FOUND);
        }

        List<PreferredTravelTheme> rows = themes.stream()
                .map(theme -> PreferredTravelTheme.builder()
                        .member(member)
                        .travelTheme(theme)
                        .build())
                .toList();

        preferredTravelThemeRepository.saveAll(rows);
        return themes;
    }
}
