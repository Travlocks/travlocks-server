package org.umc.travlocksserver.domain.auth.oauth.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverTokenResponse(
	@JsonProperty("access_token")
	String accessToken,
	@JsonProperty("refresh_token")
	String refreshToken,
	@JsonProperty("token_type")
	String tokenType,
	@JsonProperty("expires_in")
	String expiresIn,
	String error,
	@JsonProperty("error_description")
	String errorDescription) {
}
