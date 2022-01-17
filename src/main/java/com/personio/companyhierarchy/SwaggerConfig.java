package com.personio.companyhierarchy;

import java.util.ArrayList;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
				.group("springshop-public")
				.pathsToMatch("/personio/**")

				.build();
	}
	@Bean
	public GroupedOpenApi adminApi() {
		return GroupedOpenApi.builder()
				.group("springshop-admin")
				.pathsToMatch("/personio/**")
				.build();
	}
	@Bean
	public OpenAPI springShopOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("Personio - Ever-changing Hierarchy")
						.description("This services enable the company's hierarchy management")
						.version("v0.0.1")
						.license(new License().name("Apache 2.0").url("http://springdoc.org")));
//				.externalDocs(new ExternalDocumentation()
//						.description("SpringShop Wiki Documentation")
//						.url("https://springshop.wiki.github.org/docs"));
	}
}
