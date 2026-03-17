package net.chrisrichardson.ftgo.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Shared OpenAPI 3.0 configuration for all FTGO microservices.
 *
 * <p>Replaces the deprecated Springfox/Swagger 2.x configuration with
 * SpringDoc OpenAPI 3.0. Each service can customize the title and description
 * via application properties.</p>
 *
 * <p>Swagger UI is available at {@code /swagger-ui.html} for each service.</p>
 */
@Configuration
public class FtgoOpenApiConfiguration {

    @Value("${ftgo.openapi.title:FTGO API}")
    private String title;

    @Value("${ftgo.openapi.description:Food To Go Microservices Platform API}")
    private String description;

    @Value("${ftgo.openapi.version:1.0.0}")
    private String version;

    @Value("${ftgo.openapi.contact.name:FTGO Team}")
    private String contactName;

    @Value("${ftgo.openapi.contact.email:team@ftgo.io}")
    private String contactEmail;

    @Value("${ftgo.openapi.contact.url:https://ftgo.io}")
    private String contactUrl;

    @Bean
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
                .servers(List.of(
                        new Server().url("/").description("Default Server")));
    }
}
