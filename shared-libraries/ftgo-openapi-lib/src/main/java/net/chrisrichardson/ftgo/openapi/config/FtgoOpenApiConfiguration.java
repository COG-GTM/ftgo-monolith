package net.chrisrichardson.ftgo.openapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 configuration for FTGO microservices.
 *
 * <p>Provides a fully configured {@link OpenAPI} bean with:
 * <ul>
 *   <li>API metadata (title, version, description, contact)</li>
 *   <li>JWT Bearer security scheme (when enabled)</li>
 *   <li>Standard error responses applied globally</li>
 * </ul>
 *
 * <p>Services include this library as a dependency and customise behaviour
 * through {@link FtgoOpenApiProperties} in their application configuration.
 */
@Configuration
@EnableConfigurationProperties(FtgoOpenApiProperties.class)
public class FtgoOpenApiConfiguration {

    private final FtgoOpenApiProperties properties;

    public FtgoOpenApiConfiguration(FtgoOpenApiProperties properties) {
        this.properties = properties;
    }

    /**
     * Builds the root {@link OpenAPI} specification.
     */
    @Bean
    public OpenAPI ftgoOpenApi() {
        OpenAPI openApi = new OpenAPI()
                .info(buildInfo())
                .components(new Components());

        if (properties.isSecurityEnabled()) {
            openApi.components(openApi.getComponents()
                    .addSecuritySchemes("bearerAuth", new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT access token obtained from the authentication endpoint")));
            openApi.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }

        return openApi;
    }

    /**
     * Groups all controllers under the configured base package into a
     * versioned API group (e.g., "v1").
     */
    @Bean
    public GroupedOpenApi ftgoApiGroup() {
        return GroupedOpenApi.builder()
                .group(properties.getVersion())
                .packagesToScan(properties.getBasePackage())
                .addOpenApiCustomiser(standardResponseCustomiser())
                .build();
    }

    /**
     * Customiser that adds standard error responses to every operation.
     */
    @Bean
    public OpenApiCustomiser standardResponseCustomiser() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses responses = operation.getResponses();
                    addStandardErrorResponses(responses);
                })
        );
    }

    private Info buildInfo() {
        Info info = new Info()
                .title(properties.getTitle())
                .version(properties.getVersion())
                .description(properties.getDescription());

        if (!properties.getContactName().isEmpty()) {
            Contact contact = new Contact()
                    .name(properties.getContactName());
            if (!properties.getContactEmail().isEmpty()) {
                contact.email(properties.getContactEmail());
            }
            info.contact(contact);
        }

        if (!properties.getTermsOfServiceUrl().isEmpty()) {
            info.termsOfService(properties.getTermsOfServiceUrl());
        }

        if (!properties.getLicenseName().isEmpty()) {
            License license = new License().name(properties.getLicenseName());
            if (!properties.getLicenseUrl().isEmpty()) {
                license.url(properties.getLicenseUrl());
            }
            info.license(license);
        }

        return info;
    }

    @SuppressWarnings("rawtypes")
    private void addStandardErrorResponses(ApiResponses responses) {
        Schema errorSchema = new Schema().$ref("#/components/schemas/ErrorResponse");
        Content jsonContent = new Content()
                .addMediaType("application/json", new MediaType().schema(errorSchema));

        if (responses.get("400") == null) {
            responses.addApiResponse("400", new ApiResponse()
                    .description("Bad Request – validation failed or malformed input")
                    .content(jsonContent));
        }
        if (responses.get("401") == null) {
            responses.addApiResponse("401", new ApiResponse()
                    .description("Unauthorized – missing or invalid authentication token")
                    .content(jsonContent));
        }
        if (responses.get("403") == null) {
            responses.addApiResponse("403", new ApiResponse()
                    .description("Forbidden – insufficient permissions")
                    .content(jsonContent));
        }
        if (responses.get("500") == null) {
            responses.addApiResponse("500", new ApiResponse()
                    .description("Internal Server Error")
                    .content(jsonContent));
        }
    }
}
