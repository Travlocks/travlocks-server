package org.umc.travlocksserver.domain.auth.oauth.google;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleIdTokenVerifier {

	private static final List<String> ALLOWED_ISS = List.of(
		"https://accounts.google.com",
		"accounts.google.com");

	private final GoogleOAuthProperties props;

	public Jwt verify(String idToken) {
		// Google 공개키로 서명 검증
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(props.getJwkSetUri()).build();

		// 기본 검증 + aud 검증
		OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
			JwtValidators.createDefault(),
			new AudienceValidator(props.getClientId()));
		decoder.setJwtValidator(validator);

		Jwt jwt = decoder.decode(idToken);

		// issuer 검증
		String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
		if (iss == null || ALLOWED_ISS.stream().noneMatch(iss::equals)) {
			throw new JwtValidationException(
				"Invalid issuer",
				List.of(new OAuth2Error("invalid_token", "Invalid issuer", null)));
		}

		return jwt;
	}

	/**
	 * aud 검증
	 */
	static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
		private final String clientId;

		AudienceValidator(String clientId) {
			this.clientId = clientId;
		}

		@Override
		public OAuth2TokenValidatorResult validate(Jwt token) {
			if (token.getAudience() != null && token.getAudience().contains(clientId)) {
				return OAuth2TokenValidatorResult.success();
			}
			return OAuth2TokenValidatorResult.failure(
				new OAuth2Error("invalid_token", "Invalid audience", null));
		}
	}
}
