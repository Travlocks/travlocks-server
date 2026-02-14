package org.umc.travlocksserver.global.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.profile")
public record DefaultProfileImageProperties(
	String defaultBaseUrl,
	List<String> defaultFiles) {
}
