package org.umc.travlocksserver.global.apiPayload.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.umc.travlocksserver.global.apiPayload.config.AwsProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;


@Configuration
@RequiredArgsConstructor
public class SesConfig {

    private final AwsProperties awsProperties;

    @Bean
    public SesClient sesClient() {
        String region = awsProperties.region();
        String accessKey = awsProperties.credentials() != null ? awsProperties.credentials().accessKey() : null;
        String secretKey = awsProperties.credentials() != null ? awsProperties.credentials().secretKey() : null;

        // access/secret이 둘 다 있으면: 로컬/개발용 키로 동작
        if (hasText(accessKey) && hasText(secretKey)) {
            return SesClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
                    )
                    .build();
        }

        // 키가 없으면: 기본 자격 증명 체인 사용 (EC2 IAM Role, AWS CLI 프로필 등)
        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
