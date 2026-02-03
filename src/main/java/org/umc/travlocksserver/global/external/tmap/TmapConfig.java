package org.umc.travlocksserver.global.external.tmap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

@Getter
@Configuration
public class TmapConfig {

	@Value("${tmap.api.key}")
	private String apiKey;

	@Value("${tmap.api.base-url:https://apis.openapi.sk.com}")
	private String baseUrl;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}