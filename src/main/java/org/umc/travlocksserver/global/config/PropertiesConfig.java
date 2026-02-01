package org.umc.travlocksserver.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.umc.travlocksserver.global.profile.DefaultProfileImageProperties;

@Configuration
@EnableConfigurationProperties(DefaultProfileImageProperties.class)
public class PropertiesConfig {}
