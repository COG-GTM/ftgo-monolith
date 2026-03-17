package net.chrisrichardson.ftgo.openapi.config;

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
 * Auto-configuration for OpenAPI 3.0 documentation across all FTGO services.
 *
 * <p>Provides a default {@link OpenAPI} bean with standard metadata that can be
 * customized per-service via application properties:
 * <ul>
 *   <li>{@code ftgo.openapi.title} - API title (default: FTGO API)</li>
 *   <li>{@code ftgo.openapi.description} - API description</li>
 *   <li>{@code ftgo.openapi.version} - API version (default: 1.0.0)</li>
 * </ul>
 *
 * <p>Migrated from Springfox Swagger 2.x to springdoc-openapi (OpenAPI 3.0).
 */
@Configuration
public class FtgoOpenApiAutoConfiguration {

    @Value("${ftgo.openapi.title:FTGO API}")
    private String title;

    @Value("${ftgo.openapi.description:Food To Go Microservices Platform API}")
    private String description;

    @Value("${ftgo.openapi.version:1.0.0}")
    private String version;

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI ftgoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name("FTGO Platform Team")
                                .email("platform@ftgo.io")
                                .url("https://github.com/COG-GTM/ftgo-monolith"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Current Server")));
    }
}
