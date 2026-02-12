package org.umc.travlocksserver.domain.auth.dto.response;

public record AuthLoginResponseDTO(
	Long memberId,
	String accessToken,
	long accessTokenExpiresIn) {
}
