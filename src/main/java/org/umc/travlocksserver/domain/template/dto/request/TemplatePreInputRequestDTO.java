package org.umc.travlocksserver.domain.template.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.umc.travlocksserver.domain.template.enums.TripDays;

import java.util.List;

public record TemplatePreInputRequestDTO(

        @NotEmpty(message = "여행지는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 2, message = "여행지는 최대 2개까지 선택할 수 있습니다.")
        List<Long> destinationCityIds,

        @NotNull(message = "여행 기간 정보는 필수입니다.")
        @Valid
        TripDays tripDays,

        @NotEmpty(message = "교통수단은 최소 1개 이상 선택해야 합니다.")
        List<String> transportTypes,

        @NotEmpty(message = "여행 테마는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 3, message = "여행 테마는 최대 3개까지 선택할 수 있습니다.")
        List<Long> travelThemeIds

) {
}