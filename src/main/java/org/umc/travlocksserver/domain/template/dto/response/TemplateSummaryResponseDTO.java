package org.umc.travlocksserver.domain.template.dto.response;

import java.util.List;

public record TemplateSummaryResponseDTO(
        Long templateId,
        Integer totalVlocks,
        Double totalStayHours,
        Integer totalMoveMinutes,
        List<DaySummaryDTO> daysSummary
) {
    public record DaySummaryDTO(
            Long templateDayId,
            Integer dayNo,
            Integer vlockCount,
            Double stayHours,
            Integer moveMinutes,
            List<String> warnings
    ) {}
}