package net.chrisrichardson.ftgo.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Auto-configuration for SpringDoc OpenAPI 3.0 in FTGO microservices.
 *
 * <p>This configuration provides default OpenAPI metadata (title, version, description,
 * contact info) and Swagger UI integration. Services that depend on {@code ftgo-openapi-lib}
 * automatically get Swagger UI available at {@code /swagger-ui.html}.</p>
 *
 * <h3>Customization</h3>
 * <p>Services can override defaults via {@code application.properties} or
 * {@code application.yml}:</p>
 * <pre>
 * ftgo.openapi.title=My Service API
 * ftgo.openapi.version=1.0.0
 * ftgo.openapi.description=My service description
 * ftgo.openapi.base-package=net.chrisrichardson.ftgo.myservice
 * </pre>
 *
 * <p>Alternatively, services can define their own {@link OpenAPI} bean to fully
 * override the default configuration.</p>
 *
 * @see <a href="https://springdoc.org/">SpringDoc OpenAPI Documentation</a>
 */
@Configuration
public class FtgoOpenApiAutoConfiguration {

    @Value("${ftgo.openapi.title:FTGO Service API}")
    private String title;

    @Value("${ftgo.openapi.version:1.0.0}")
    private String version;

    @Value("${ftgo.openapi.description:FTGO Microservice REST API}")
    private String description;

    @Value("${ftgo.openapi.contact.name:FTGO Team}")
    private String contactName;

    @Value("${ftgo.openapi.contact.email:ftgo-team@example.com}")
    private String contactEmail;

    @Value("${ftgo.openapi.contact.url:https://github.com/COG-GTM/ftgo-monolith}")
    private String contactUrl;

    @Value("${ftgo.openapi.base-package:net.chrisrichardson.ftgo}")
    private String basePackage;

    /**
     * Creates the default OpenAPI metadata bean.
     *
     * <p>This bean is only created if the consuming service has not already defined
     * its own {@link OpenAPI} bean, allowing full customization when needed.</p>
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI ftgoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Default Server")));
    }

    /**
     * Creates a grouped OpenAPI definition scoped to the versioned API path.
     *
     * <p>This groups all endpoints under {@code /api/v1/**} into a single
     * API group, following the FTGO URL path versioning strategy.</p>
     *
     * @return the configured {@link GroupedOpenApi} for v1 endpoints
     */
    @Bean
    @ConditionalOnMissingBean(GroupedOpenApi.class)
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .packagesToScan(basePackage)
                .build();
    }
}
