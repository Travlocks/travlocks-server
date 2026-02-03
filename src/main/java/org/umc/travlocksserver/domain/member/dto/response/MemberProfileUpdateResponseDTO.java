package org.umc.travlocksserver.domain.member.dto.response;

import java.util.List;

public record MemberProfileUpdateResponseDTO(
        Long memberId,
        String nickname,
        String introduction,
        List<Long> preferredTravelStyleIds,
        List<Long> preferredTravelThemeIds
) {
}