package com.ftgo.openapi.config;

import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI configuration for FTGO microservices.
 *
 * <p>SpringDoc automatically configures Swagger UI. This configuration class
 * serves as an extension point for custom Swagger UI behavior.
 *
 * <h3>Swagger UI Endpoints (SpringDoc defaults)</h3>
 * <ul>
 *   <li>{@code /swagger-ui/index.html} - Swagger UI interface</li>
 *   <li>{@code /swagger-ui.html} - Redirect to Swagger UI (backward-compatible)</li>
 *   <li>{@code /v3/api-docs} - OpenAPI 3.0 JSON spec</li>
 *   <li>{@code /v3/api-docs.yaml} - OpenAPI 3.0 YAML spec</li>
 * </ul>
 *
 * <h3>Configuration Properties</h3>
 * <p>Swagger UI can be customized via application.yml:
 * <pre>
 * springdoc:
 *   swagger-ui:
 *     path: /swagger-ui.html
 *     tags-sorter: alpha
 *     operations-sorter: alpha
 *     display-request-duration: true
 *     filter: true
 *   api-docs:
 *     path: /v3/api-docs
 *   show-actuator: false
 *   packages-to-scan: net.chrisrichardson.ftgo
 *   paths-to-match: /api/**
 * </pre>
 *
 * <h3>Migration from Springfox</h3>
 * <p>The old Springfox Swagger UI was available at {@code /swagger-ui.html}.
 * SpringDoc provides the same URL as a redirect for backward compatibility.
 * The new native URL is {@code /swagger-ui/index.html}.
 */
@Configuration
public class FtgoOpenApiSwaggerUiConfiguration {
    // SpringDoc auto-configures Swagger UI out of the box.
    // This class serves as an extension point for future customizations
    // such as custom CSS, authorization configuration, or UI plugins.
}
