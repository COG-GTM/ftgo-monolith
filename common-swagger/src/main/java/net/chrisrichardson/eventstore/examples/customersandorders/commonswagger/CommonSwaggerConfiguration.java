package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Common OpenAPI/Swagger configuration for FTGO services.
 * Migrated from Springfox (Spring Boot 2.x) to springdoc-openapi (Spring Boot 3.x).
 */
@Configuration
public class CommonSwaggerConfiguration {

    @Bean
    public OpenAPI ftgoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FTGO API")
                        .description("Food To Go Microservices Platform API")
                        .version("1.0.0"));
    }
}
