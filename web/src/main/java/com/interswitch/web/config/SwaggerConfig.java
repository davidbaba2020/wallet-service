package com.interswitch.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\""
)
public class SwaggerConfig {

    @Value("${openapi.service.localUrl:http://localhost:8088/api/v1}")
    private String localUrl;

    @Value("${openapi.service.devUrl:https://dev-api.interswitch.com/api/v1}")
    private String devUrl;

    @Value("${openapi.service.prodUrl:https://api.interswitch.com/api/v1}")
    private String prodUrl;

    @Value("${openapi.service.title:Wallet Service API}")
    private String title;

    @Value("${openapi.service.version:1.0.0}")
    private String version;

    @Value("${openapi.service.description:Comprehensive Wallet Management Service}")
    private String description;

    private io.swagger.v3.oas.models.security.SecurityScheme createBearerAuthScheme() {
        return new io.swagger.v3.oas.models.security.SecurityScheme()
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                .description("JWT token for accessing protected endpoints");
    }

    private io.swagger.v3.oas.models.security.SecurityScheme createApiKeyScheme() {
        return new io.swagger.v3.oas.models.security.SecurityScheme()
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("API Key for service-to-service communication");
    }

    @Bean
    public OpenAPI walletServiceOpenAPI() {
        log.info("Configuring Swagger OpenAPI for Wallet Service with context path /api/v1");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", createBearerAuthScheme())
                        .addSecuritySchemes("apiKey", createApiKeyScheme()))
                .security(List.of(
                        new SecurityRequirement().addList("bearerAuth"),
                        new SecurityRequirement().addList("apiKey")
                ))
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .termsOfService("https://interswitch.com/terms")
                        .contact(new Contact()
                                .name("Interswitch API Team")
                                .email("api-support@interswitch.com")
                                .url("https://interswitch.com/contact"))
                        .license(new License()
                                .name("Interswitch API License")
                                .url("https://interswitch.com/license")))
                .servers(List.of(
                        new Server().url(localUrl).description("Local Development Server"),
                        new Server().url(devUrl).description("Development Environment"),
                        new Server().url(prodUrl).description("Production Environment")
                ))
                .tags(List.of(
                        new Tag().name("Wallets").description("Wallet management operations"),
                        new Tag().name("Balances").description("Wallet balance operations"),
                        new Tag().name("Transactions").description("Transaction management"),
                        new Tag().name("Limits").description("Wallet limit management"),
                        new Tag().name("Freezes").description("Wallet freeze operations"),
                        new Tag().name("Settings").description("Wallet settings management"),
                        new Tag().name("Audit").description("Audit log operations"),
                        new Tag().name("Health").description("Service health and monitoring")
                ));
    }

    @Bean
    public GroupedOpenApi walletManagementApi() {
        return GroupedOpenApi.builder()
                .group("wallet-management")
                .displayName("Wallet Management")
                .pathsToMatch("/wallets/**")  // Removed /api/v1 since it's in context path
                .packagesToScan("com.interswitch.web.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi balanceManagementApi() {
        return GroupedOpenApi.builder()
                .group("balance-management")
                .displayName("Balance Management")
                .pathsToMatch("/wallet-balances/**")
                .packagesToScan("com.interswitch.web.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi transactionManagementApi() {
        return GroupedOpenApi.builder()
                .group("transaction-management")
                .displayName("Transaction Management")
                .pathsToMatch("/wallet-transactions/**")
                .packagesToScan("com.interswitch.web.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi limitManagementApi() {
        return GroupedOpenApi.builder()
                .group("limit-management")
                .displayName("Limit Management")
                .pathsToMatch("/wallet-limits/**")
                .packagesToScan("com.interswitch.web.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi freezeManagementApi() {
        return GroupedOpenApi.builder()
                .group("freeze-management")
                .displayName("Freeze Management")
                .pathsToMatch("/wallet-freezes/**")
                .packagesToScan("com.interswitch.web.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi settingsManagementApi() {
        return GroupedOpenApi.builder()
                .group("settings-management")
                .displayName("Settings Management")
                .pathsToMatch("/wallet-settings/**")
                .packagesToScan("com.interswitch.web.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi auditManagementApi() {
        return GroupedOpenApi.builder()
                .group("audit-management")
                .displayName("Audit Management")
                .pathsToMatch("/wallet-audit-logs/**")
                .packagesToScan("com.interswitch.web.controllers")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Public APIs")
                .pathsToMatch("/actuator/**", "/public/**")  // Removed /api since actuator is outside context path
                .pathsToExclude("/actuator/health/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApisGroup() {
        return GroupedOpenApi.builder()
                .group("all-apis")
                .displayName("All APIs")
                .pathsToMatch("/**")  // Match all paths within context
                .packagesToScan("com.interswitch.web.controllers")
                .build();
    }
}