package org.umc.travlocksserver.domain.auth.oauth.google;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth.google")
public class GoogleOAuthProperties {
    private String clientId;
    private String jwkSetUri;
}

