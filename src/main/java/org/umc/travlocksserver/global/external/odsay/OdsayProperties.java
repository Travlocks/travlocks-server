package org.umc.travlocksserver.global.external.odsay;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "odsay")
public record OdsayProperties(String apiKey) {}
