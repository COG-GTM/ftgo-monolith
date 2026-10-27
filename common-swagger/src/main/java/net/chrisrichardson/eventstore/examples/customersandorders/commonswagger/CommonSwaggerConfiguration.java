package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

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

}
