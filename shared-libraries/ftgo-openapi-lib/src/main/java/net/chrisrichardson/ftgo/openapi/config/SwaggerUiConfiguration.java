package net.chrisrichardson.ftgo.openapi.config;

import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI configuration for FTGO services.
 *
 * <p>SpringDoc automatically serves Swagger UI at {@code /swagger-ui.html}
 * when the {@code springdoc-openapi-starter-webmvc-ui} dependency is on the classpath.
 *
 * <p>Default endpoints provided by SpringDoc:
 * <ul>
 *   <li>{@code /swagger-ui.html} - Swagger UI (redirects to /swagger-ui/index.html)</li>
 *   <li>{@code /swagger-ui/index.html} - Swagger UI main page</li>
 *   <li>{@code /v3/api-docs} - OpenAPI 3.0 spec in JSON format</li>
 *   <li>{@code /v3/api-docs.yaml} - OpenAPI 3.0 spec in YAML format</li>
 * </ul>
 *
 * <p>These can be customized per-service via {@code application.properties}:
 * <pre>
 * # Custom Swagger UI path
 * springdoc.swagger-ui.path=/swagger-ui.html
 *
 * # Enable/disable Swagger UI
 * springdoc.swagger-ui.enabled=true
 *
 * # API docs path
 * springdoc.api-docs.path=/v3/api-docs
 *
 * # Sort operations alphabetically
 * springdoc.swagger-ui.operationsSorter=alpha
 * springdoc.swagger-ui.tagsSorter=alpha
 * </pre>
 */
@Configuration
public class SwaggerUiConfiguration {
    // SpringDoc auto-configures Swagger UI when on classpath.
    // This class serves as a marker and documentation reference.
    // Per-service customization should be done via application.properties.
}
