package org.umc.travlocksserver.domain.template.dto.response;

import org.umc.travlocksserver.domain.template.entity.TemplateVlock;
import org.umc.travlocksserver.domain.template.enums.ConnectionPortType;
import org.umc.travlocksserver.domain.vlock.entity.Vlock;

import java.time.LocalDateTime;

public record TemplateVlockAddResponseDTO(
        Long templateVlocksId,
        Long templateDayId,
        Integer dayNo,
        Integer orderNo,
        Double stayHours,
        Double canvasX,
        Double canvasY,
        ConnectionPortType inputPort,
        ConnectionPortType outputPort,
        VlockDetailDTO vlock,
        LocalDateTime createdAt,
        String warning  //  경고 메시지 추가 (블록 개수 초과 등)
) {
    public static TemplateVlockAddResponseDTO from(TemplateVlock tv, String warning) {
        return new TemplateVlockAddResponseDTO(
                tv.getId(),
                tv.getTemplateDay().getId(),
                tv.getTemplateDay().getDayNo(),
                tv.getOrderNo(),
                tv.getStayHours(),
                tv.getCanvasX(),
                tv.getCanvasY(),
                tv.getInputPort(),
                tv.getOutputPort(),
                VlockDetailDTO.from(tv.getVlock()),
                tv.getCreatedAt(),
                warning
        );
    }

    public record VlockDetailDTO(
            Long vlockId,
            String name,
            Long vlockCategoryId,
            String categoryName,
            String coverImgUrl,
            String address,
            Double latitude,
            Double longitude
    ) {
        public static VlockDetailDTO from(Vlock v) {
            return new VlockDetailDTO(
                    v.getId(),
                    v.getName(),
                    v.getVlockCategory().getId(),
                    v.getVlockCategory().getName(),
                    v.getCoverImgUrl(),
                    v.getAddress(),
                    v.getLatitude(),
                    v.getLongitude()
            );
        }
    }
}