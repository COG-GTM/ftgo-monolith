package com.ftgo.security.config;

import com.ftgo.security.authorization.FtgoAuthorizationAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for the FTGO security library.
 * <p>
 * Import this configuration in any microservice to enable the full
 * FTGO security stack including:
 * <ul>
 *   <li>Base security configuration (stateless sessions, CSRF disabled, CORS)</li>
 *   <li>JWT authentication (when {@code ftgo.security.jwt.enabled=true})</li>
 *   <li>Role-based authorization with role hierarchy</li>
 *   <li>Method-level security ({@code @PreAuthorize}, {@code @Secured})</li>
 *   <li>Custom permission evaluator for ownership checks</li>
 * </ul>
 * <pre>
 * &#64;Import(FtgoSecurityAutoConfiguration.class)
 * &#64;SpringBootApplication
 * public class MyServiceApplication { }
 * </pre>
 * Or rely on component scanning if the service scans {@code com.ftgo.security}.
 * </p>
 *
 * @see FtgoBaseSecurityConfiguration
 * @see FtgoAuthorizationAutoConfiguration
 */
@Configuration
@ComponentScan(basePackages = "com.ftgo.security")
@Import({
        FtgoBaseSecurityConfiguration.class,
        FtgoCorsConfiguration.class,
        FtgoAuthorizationAutoConfiguration.class
})
public class FtgoSecurityAutoConfiguration {
}
