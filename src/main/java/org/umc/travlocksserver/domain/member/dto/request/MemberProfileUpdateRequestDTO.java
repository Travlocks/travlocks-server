package org.umc.travlocksserver.domain.member.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

public record MemberProfileUpdateRequestDTO(
	JsonNullable<@Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다.") @Pattern(regexp = "^[가-힣a-zA-Z]{2,10}$", message = "닉네임은 한글/영문만 가능합니다.") String> nickname,
	JsonNullable<@Size(max = 500, message = "소개는 최대 500자입니다.") String> introduction,
	JsonNullable<List<Long>> preferredTravelStyleIds,
	JsonNullable<List<Long>> preferredTravelThemeIds) {
}
