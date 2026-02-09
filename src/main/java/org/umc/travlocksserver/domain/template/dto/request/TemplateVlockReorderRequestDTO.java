package org.umc.travlocksserver.domain.template.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.umc.travlocksserver.domain.template.enums.ConnectionPortType;

import java.util.List;

public record TemplateVlockReorderRequestDTO(
        @NotEmpty(message = "블록 순서 정보는 필수입니다.")
        List<@Valid VlockOrder> vlockOrders
) {
    public record VlockOrder(
            @NotNull(message = "블록 ID는 필수입니다.")
            Long templateVlocksId,

            @NotNull(message = "순서는 필수입니다.")
            Integer orderNo,

            Double canvasX,

            Double canvasY,

            ConnectionPortType inputPort,   // 이전 블록에서 받는 포트

            ConnectionPortType outputPort   // 다음 블록으로 보내는 포트
    ) {}
}