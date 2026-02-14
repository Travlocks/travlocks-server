package org.umc.travlocksserver.domain.member.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MemberOAuthOnboardingRequestDTO(
	@NotBlank
	String nickname,
	@NotNull @Valid
	List<ConsentDTO> consents,
	@NotNull
	List<Long> preferredTravelStyleIds,
	@NotNull
	List<Long> preferredTravelThemeIds) {
	public record ConsentDTO(
		@NotNull
		Long policyId,
		@NotNull
		Boolean agreed) {
	}
}
