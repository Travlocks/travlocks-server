package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.template.entity.TemplateVlock;

import java.util.List;

public record TemplateVlockDeleteResponseDTO(
        Long deletedTemplateVlocksId,
        Long templateDayId,
        Integer dayNo,
        List<RemainingVlock> remainingVlocks
) {
    public static TemplateVlockDeleteResponseDTO from(
            Long deletedId,
            Long templateDayId,
            Integer dayNo,
            List<TemplateVlock> remainingVlocks
    ) {
        List<RemainingVlock> vlocks = remainingVlocks.stream()
                .map(tv -> new RemainingVlock(
                        tv.getId(),
                        tv.getOrderNo(),
                        tv.getVlock().getName()
                ))
                .toList();

        return new TemplateVlockDeleteResponseDTO(
                deletedId,
                templateDayId,
                dayNo,
                vlocks
        );
    }

    public record RemainingVlock(
            Long templateVlocksId,
            Integer orderNo,
            String vlockName
    ) {}
}