package org.umc.travlocksserver.global.apiPayload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	private static final String JWT_SCHEME_NAME = "JWT";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(apiInfo())
			.components(components())
			.addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME_NAME));
	}

	private Info apiInfo() {
		return new Info()
			.title("API documents")
			.description("travlocks API 명세서입니다.")
			.version("1.0.0");
	}

	private Components components() {
		SecurityScheme jwtSecurityScheme = new SecurityScheme()
			.name(JWT_SCHEME_NAME)
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT");

		return new Components()
			.addSecuritySchemes(JWT_SCHEME_NAME, jwtSecurityScheme);
	}
}
