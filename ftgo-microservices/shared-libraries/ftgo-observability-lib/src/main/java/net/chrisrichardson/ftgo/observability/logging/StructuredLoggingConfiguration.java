package net.chrisrichardson.ftgo.observability.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class StructuredLoggingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(StructuredLoggingConfiguration.class);

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @PostConstruct
    public void init() {
        System.setProperty("SERVICE_NAME", serviceName);
        log.info("Structured logging initialized for service: {}", serviceName);
    }
}
