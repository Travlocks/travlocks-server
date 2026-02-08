package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.enums.ConnectionPortType;

import java.util.List;

public record TemplateVlockReorderResponseDTO(
        Long templateDayId,
        Integer dayNo,
        List<ReorderedVlock> vlocks,
        List<String> warnings  // ✨ 여러 경고 메시지
) {
    public static TemplateVlockReorderResponseDTO from(
            Long templateDayId,
            Integer dayNo,
            List<TemplateVlock> vlocks,
            List<String> warnings
    ) {
        List<ReorderedVlock> reorderedVlocks = vlocks.stream()
                .map(ReorderedVlock::from)
                .toList();

        return new TemplateVlockReorderResponseDTO(
                templateDayId,
                dayNo,
                reorderedVlocks,
                warnings
        );
    }

    public record ReorderedVlock(
            Long templateVlocksId,
            Integer orderNo,
            Double stayHours,
            Double canvasX,
            Double canvasY,
            ConnectionPortType connectionPort,
            VlockBrief vlock,
            MoveToNext moveToNext
    ) {
        public static ReorderedVlock from(TemplateVlock tv) {
            return new ReorderedVlock(
                    tv.getId(),
                    tv.getOrderNo(),
                    tv.getStayHours(),
                    tv.getCanvasX(),
                    tv.getCanvasY(),
                    tv.getConnectionPort(),
                    new VlockBrief(
                            tv.getVlock().getId(),
                            tv.getVlock().getName(),
                            tv.getVlock().getCoverImgUrl()
                    ),
                    null  // Service에서 계산
            );
        }

        public record VlockBrief(
                Long vlockId,
                String name,
                String coverImgUrl
        ) {}

        public record MoveToNext(
                Integer moveMinutes,
                String transportType,
                Integer distanceMeter
        ) {}
    }
}