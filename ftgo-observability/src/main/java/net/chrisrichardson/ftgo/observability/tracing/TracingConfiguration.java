package net.chrisrichardson.ftgo.observability.tracing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfiguration {

    @Value("${spring.application.name:ftgo-application}")
    private String applicationName;

    @Bean
    public TracingContextProvider tracingContextProvider() {
        return new TracingContextProvider(applicationName);
    }
}
