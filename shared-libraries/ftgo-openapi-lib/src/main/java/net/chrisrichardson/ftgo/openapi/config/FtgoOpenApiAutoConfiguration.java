package net.chrisrichardson.ftgo.openapi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot auto-configuration for FTGO OpenAPI support.
 *
 * <p>Automatically activates when SpringDoc is on the classpath and
 * {@code ftgo.openapi.enabled} is not explicitly set to {@code false}.
 *
 * <p>Register this class in {@code META-INF/spring.factories} under
 * {@code org.springframework.boot.autoconfigure.EnableAutoConfiguration}.
 */
@Configuration
@ConditionalOnClass(name = "org.springdoc.core.GroupedOpenApi")
@ConditionalOnProperty(prefix = "ftgo.openapi", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(FtgoOpenApiConfiguration.class)
public class FtgoOpenApiAutoConfiguration {
}
