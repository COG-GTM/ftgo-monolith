package com.ftgo.template.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Service-specific security configuration for the template microservice.
 * <p>
 * Imports the shared FTGO security auto-configuration which provides:
 * <ul>
 *   <li>Stateless session management</li>
 *   <li>CSRF disabled for REST APIs</li>
 *   <li>CORS configured for API gateway</li>
 *   <li>Public actuator health/info endpoints</li>
 *   <li>All other endpoints require authentication</li>
 *   <li>JSON error responses for 401/403</li>
 * </ul>
 * </p>
 * <p>
 * To customize security for a specific service, add additional
 * {@link org.springframework.security.web.SecurityFilterChain} beans here
 * or extend the public paths via {@code ftgo.security.public-paths} in
 * application.properties.
 * </p>
 */
@Configuration
@Import(com.ftgo.security.config.FtgoSecurityAutoConfiguration.class)
public class ServiceSecurityConfiguration {
    // Service-specific security customizations can be added here.
    // The base configuration from ftgo-security-lib handles defaults.
}
