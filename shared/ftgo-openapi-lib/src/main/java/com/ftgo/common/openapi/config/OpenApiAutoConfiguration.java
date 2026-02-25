package com.ftgo.common.openapi.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot auto-configuration entry point for the FTGO OpenAPI library.
 *
 * <p>Automatically imports {@link OpenApiConfiguration} when the library
 * is on the classpath. Microservices only need to add the dependency;
 * no explicit {@code @Import} is required.
 *
 * <p>Registered via {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 */
@AutoConfiguration
@Import(OpenApiConfiguration.class)
public class OpenApiAutoConfiguration {
}
