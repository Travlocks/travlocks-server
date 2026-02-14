package org.umc.travlocksserver.global.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
	String region,
	Credentials credentials) {
	public record Credentials(String accessKey, String secretKey) {
	}
}
