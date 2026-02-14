package org.umc.travlocksserver.domain.template.dto.request;

import jakarta.validation.constraints.NotNull;
import org.umc.travlocksserver.domain.template.enums.ConnectionPortType;

public record TemplateVlockAddRequestDTO(
	@NotNull(message = "블록 ID는 필수입니다.")
	Long vlockId,

	Double canvasX,

	Double canvasY,

	ConnectionPortType inputPort,

	ConnectionPortType outputPort

) {
}
