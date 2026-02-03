package org.umc.travlocksserver.global.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class KakaoWebClientConfig {

    @Bean
    WebClient kakaoWebClient(
            @Value("${kakao.base-url}") String baseUrl,
            @Value("${kakao.rest-api-key}") String restKey,
            @Value("${kakao.timeout.connect-ms}") int connectTimeoutMs,
            @Value("${kakao.timeout.response-ms}") int responseTimeoutMs
    ) {
        // 타임아웃 설정
        HttpClient http = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + restKey)
                .build();
    }
}
