package org.umc.travlocksserver.domain.member.service.command;

import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.travlocksserver.domain.member.dto.request.MemberProfileUpdateRequestDTO;
import org.umc.travlocksserver.domain.member.dto.response.MemberProfileUpdateResponseDTO;
import org.umc.travlocksserver.domain.member.entity.Member;
import org.umc.travlocksserver.domain.member.exception.MemberException;
import org.umc.travlocksserver.domain.member.code.MemberErrorCode;
import org.umc.travlocksserver.domain.member.repository.MemberRepository;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberProfileUpdateService {

    private final MemberRepository memberRepository;

    private final PreferredTravelStyleRepository preferredStyleRepository;
    private final PreferredTravelThemeRepository preferredThemeRepository;
    private final TravelStyleRepository travelStyleRepository;
    private final TravelThemeRepository travelThemeRepository;

    @Transactional
    public MemberProfileUpdateResponseDTO updateMyProfile(Member member, MemberProfileUpdateRequestDTO request) {
        Long memberId = member.getId();

        if (isPresent(request.nickname())) {
            String nickname = request.nickname().get();

            if (nickname == null || nickname.isBlank()) {
                throw new MemberException(MemberErrorCode.INVALID_NICKNAME);
            }

            if (!nickname.equals(member.getNickname())) {
                if (memberRepository.existsByNickname(nickname)) {
                    throw new MemberException(MemberErrorCode.NICKNAME_ALREADY_EXISTS);
                }
                member.changeNickname(nickname);
            }
        }

        if (isPresent(request.introduction())) {
            member.changeIntroduction(request.introduction().orElse(null));
        }

        if (isPresent(request.preferredTravelStyleIds())) {
            List<Long> styleIds = request.preferredTravelStyleIds().get();
            if (styleIds == null) {
                throw new TravelStyleException(TravelStyleErrorCode.INVALID_PREFERRED_STYLE_REQUEST);
            }

            List<Long> uniqueStyleIds = new ArrayList<>(new LinkedHashSet<>(styleIds));

            if (uniqueStyleIds.size() > 2) {
                throw new TravelStyleException(TravelStyleErrorCode.TRAVEL_STYLE_MAX_EXCEEDED);
            }

            preferredStyleRepository.deleteByMemberId(memberId);

            if (!uniqueStyleIds.isEmpty()) {
                List<TravelStyle> styles = travelStyleRepository.findAllById(uniqueStyleIds);
                if (styles.size() != uniqueStyleIds.size()) {
                    throw new TravelStyleException(TravelStyleErrorCode.TRAVEL_STYLE_NOT_FOUND);
                }

                Map<Long, TravelStyle> styleMap = styles.stream()
                        .collect(Collectors.toMap(TravelStyle::getId, Function.identity()));

                List<PreferredTravelStyle> rows = uniqueStyleIds.stream()
                        .map(styleId -> PreferredTravelStyle.builder()
                                .member(Member.of(memberId))
                                .travelStyle(styleMap.get(styleId))
                                .build())
                        .toList();

                preferredStyleRepository.saveAll(rows);
            }
        }

        if (isPresent(request.preferredTravelThemeIds())) {
            List<Long> themeIds = request.preferredTravelThemeIds().get();
            if (themeIds == null) {
                throw new TravelThemeException(TravelThemeErrorCode.INVALID_PREFERRED_THEME_REQUEST);
            }

            List<Long> uniqueThemeIds = new ArrayList<>(new LinkedHashSet<>(themeIds));

            if (uniqueThemeIds.size() > 2) {
                throw new TravelThemeException(TravelThemeErrorCode.TRAVEL_THEME_MAX_EXCEEDED);
            }

            preferredThemeRepository.deleteByMemberId(memberId);

            if (!uniqueThemeIds.isEmpty()) {
                List<TravelTheme> themes = travelThemeRepository.findAllById(uniqueThemeIds);
                if (themes.size() != uniqueThemeIds.size()) {
                    throw new TravelThemeException(TravelThemeErrorCode.TRAVEL_THEME_NOT_FOUND);
                }

                Map<Long, TravelTheme> themeMap = themes.stream()
                        .collect(Collectors.toMap(TravelTheme::getId, Function.identity()));

                List<PreferredTravelTheme> rows = uniqueThemeIds.stream()
                        .map(themeId -> PreferredTravelTheme.builder()
                                .member(Member.of(memberId))
                                .travelTheme(themeMap.get(themeId))
                                .build())
                        .toList();

                preferredThemeRepository.saveAll(rows);
            }
        }

        List<Long> finalStyleIds = preferredStyleRepository.findPreferredStyleIdsByMemberId(memberId);
        List<Long> finalThemeIds = preferredThemeRepository.findPreferredThemeIdsByMemberId(memberId);

        return new MemberProfileUpdateResponseDTO(
                member.getId(),
                member.getNickname(),
                member.getIntroduction(),
                finalStyleIds,
                finalThemeIds
        );
    }

    private static boolean isPresent(JsonNullable<?> v) {
        return v != null && v.isPresent();
    }
}
