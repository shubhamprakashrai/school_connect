package com.schoolmgmt.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration for the School Connect API.
 * Provides API metadata, JWT security scheme, and server definitions
 * that are consumed by springdoc-openapi to render Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI schoolConnectOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Connect API")
                        .description(
                                "Multi-tenant School Management System REST API. " +
                                "Provides endpoints for authentication, student management, " +
                                "teacher management, attendance tracking, exam & results, " +
                                "fee collection, timetable scheduling, notifications, " +
                                "leave management, safety reporting, and tenant administration.")
                        .version(appVersion)
                        .contact(new Contact()
                                .name("School Connect Team")
                                .email("admin@schoolmgmt.com")
                                .url("https://schoolconnect.app"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://schoolconnect.app/terms")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Enter your JWT token obtained from the /api/auth/login endpoint. " +
                                                "Format: <token>")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.schoolconnect.app/api")
                                .description("Production server")));
    }
}
