package net.chrisrichardson.ftgo.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Shared OpenAPI 3.0 configuration for all FTGO microservices.
 * <p>
 * Each service applies this auto-configuration and overrides
 * {@code ftgo.openapi.title} and {@code ftgo.openapi.description}
 * in its application.properties to customize the API docs.
 */
@AutoConfiguration
public class FtgoOpenApiConfiguration {

    @Value("${ftgo.openapi.title:FTGO Microservice API}")
    private String title;

    @Value("${ftgo.openapi.description:FTGO Microservice REST API}")
    private String description;

    @Value("${ftgo.openapi.version:v1}")
    private String version;

    @Value("${ftgo.openapi.server-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI ftgoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name("FTGO Engineering")
                                .email("engineering@ftgo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url(serverUrl).description("Current environment")));
    }
}
