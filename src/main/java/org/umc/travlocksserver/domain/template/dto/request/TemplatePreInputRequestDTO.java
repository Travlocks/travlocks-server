package org.umc.travlocksserver.domain.template.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TemplatePreInputRequestDTO(

        @NotEmpty(message = "여행지는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 2, message = "여행지는 최대 2개까지 선택할 수 있습니다.")
        List<Long> destinationCityIds,

        @NotNull(message = "여행 기간 정보는 필수입니다.")
        @Valid
        TripDTO trip,

        @NotEmpty(message = "교통수단은 최소 1개 이상 선택해야 합니다.")
        List<String> transportTypes,

        @NotEmpty(message = "여행 테마는 최소 1개 이상 선택해야 합니다.")
        @Size(max = 3, message = "여행 테마는 최대 3개까지 선택할 수 있습니다.")
        List<Long> travelThemeIds

) {
    public record TripDTO(
            @NotNull(message = "여행 일수는 필수입니다.")
            @Min(value = 1, message = "여행 일수는 최소 1일입니다.")
            @Max(value = 5, message = "여행 일수는 최대 5일입니다.")
            Integer days,

            @NotNull(message = "여행 박수는 필수입니다.")
            @Min(value = 0, message = "여행 박수는 0 이상이어야 합니다.")
            Integer nights
    ) {}
}