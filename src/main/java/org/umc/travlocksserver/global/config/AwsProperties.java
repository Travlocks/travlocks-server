package org.umc.travlocksserver.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
        String region,
        Credentials credentials,
        Ses ses
) {
    public record Credentials(String accessKey, String secretKey) {}
    public record Ses(String fromEmail) {}
}
