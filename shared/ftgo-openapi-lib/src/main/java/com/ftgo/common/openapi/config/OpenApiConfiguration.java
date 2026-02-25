package com.ftgo.common.openapi.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Shared OpenAPI 3.0 configuration for all FTGO microservices.
 *
 * <p>Replaces the legacy Springfox-based {@code CommonSwaggerConfiguration}.
 * Each microservice can override individual properties via {@code application.properties}:
 *
 * <pre>
 * ftgo.openapi.title=Order Service API
 * ftgo.openapi.description=Manages order lifecycle
 * ftgo.openapi.version=v1
 * </pre>
 *
 * <p>Microservices can also provide their own {@link OpenAPI} bean to fully
 * customize the specification.
 *
 * @see <a href="https://springdoc.org/">SpringDoc OpenAPI</a>
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${ftgo.openapi.title:FTGO Service API}")
    private String title;

    @Value("${ftgo.openapi.description:FTGO Platform Microservice API}")
    private String description;

    @Value("${ftgo.openapi.version:v1}")
    private String version;

    @Value("${ftgo.openapi.contact.name:FTGO Platform Team}")
    private String contactName;

    @Value("${ftgo.openapi.contact.email:platform@ftgo.com}")
    private String contactEmail;

    @Value("${ftgo.openapi.contact.url:https://github.com/COG-GTM/ftgo-monolith}")
    private String contactUrl;

    @Value("${ftgo.openapi.server.url:http://localhost:8080}")
    private String serverUrl;

    @Value("${ftgo.openapi.server.description:Local Development Server}")
    private String serverDescription;

    /**
     * Creates the default OpenAPI specification bean.
     *
     * <p>This bean is only created if no other {@link OpenAPI} bean is defined,
     * allowing individual microservices to provide their own customization.
     *
     * @return the configured OpenAPI specification
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI ftgoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("FTGO REST API Standards")
                        .url("https://github.com/COG-GTM/ftgo-monolith/blob/main/docs/api-standards.md"))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description(serverDescription)));
    }
}
