package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonSwaggerConfiguration {

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("ftgo")
                .packagesToScan("net.chrisrichardson.ftgo")
                .build();
    }

    @Bean
    public OpenAPI ftgoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FTGO API")
                        .version("1.0"));
    }

}
