package com.technox.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI technoXOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("TECHNO-X — College Event Management Platform API")
                        .description("Production REST API backend for Techno Group of Institutions (TGI/TIHS), Lucknow UP. " +
                                "Powers event lifecycles, role-based workflows, high-concurrency seat registration, " +
                                "digital QR entry pass issuance, and audit tracking.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TECHNO-X Central Support")
                                .email("helpdesk@technox.tgi.ac.in")
                                .url("https://technox.tgi.ac.in"))
                        .license(new License().name("Educational Use License")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
