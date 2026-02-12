package org.umc.travlocksserver.domain.auth.oauth.naver;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class NaverOAuthClient {

	private final RestClient restClient = RestClient.create();

	@Value("${oauth.naver.client-id}")
	private String clientId;

	@Value("${oauth.naver.client-secret}")
	private String clientSecret;

	@Value("${oauth.naver.redirect-uri}")
	private String redirectUri;

	@Value("${oauth.naver.token-uri}")
	private String tokenUri;

	@Value("${oauth.naver.userinfo-uri}")
	private String userInfoUri;

	public NaverTokenResponse exchangeCodeForToken(String code, String state) {
		// 토큰 요청 URL
		String url = tokenUri
			+ "?grant_type=authorization_code"
			+ "&client_id=" + encode(clientId)
			+ "&client_secret=" + encode(clientSecret)
			+ "&redirect_uri=" + encode(redirectUri)
			+ "&code=" + encode(code)
			+ "&state=" + encode(state);

		// 해당 URL로 네이버 토큰 엔드포인트 호출
		return restClient.get()
			.uri(url)
			.retrieve()
			.body(NaverTokenResponse.class);
	}

	public NaverProfileResponse fetchUserProfile(String accessToken) {
		// 반환 받은 네이버 accessToken으로 네이버 사용자 정보 API 호출
		return restClient.get()
			.uri(userInfoUri)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.retrieve()
			.body(NaverProfileResponse.class);
	}

	private String encode(String v) {
		return URLEncoder.encode(v, StandardCharsets.UTF_8);
	}
}
