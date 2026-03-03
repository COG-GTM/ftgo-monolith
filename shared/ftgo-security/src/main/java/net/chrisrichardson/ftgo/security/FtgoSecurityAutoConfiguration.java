package net.chrisrichardson.ftgo.security;

import net.chrisrichardson.ftgo.security.config.FtgoCorsConfig;
import net.chrisrichardson.ftgo.security.config.FtgoSecurityFilterChainConfig;
import net.chrisrichardson.ftgo.security.exception.SecurityExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for FTGO Security.
 *
 * <p>This is the main entry point for the FTGO security library. When included
 * as a dependency, it automatically configures:</p>
 * <ul>
 *   <li>Spring Security filter chain with sensible defaults</li>
 *   <li>CORS configuration for API gateway and frontend origins</li>
 *   <li>Custom security exception handlers (JSON error responses)</li>
 *   <li>Actuator endpoint security (health=public, others=secured)</li>
 * </ul>
 *
 * <p>To use this library, add it as a dependency and optionally configure
 * properties under the {@code ftgo.security} prefix in application.yml.</p>
 *
 * <p>This configuration is only active for servlet-based web applications.</p>
 *
 * @see FtgoSecurityProperties
 * @see FtgoSecurityFilterChainConfig
 * @see FtgoCorsConfig
 * @see SecurityExceptionHandler
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(FtgoSecurityProperties.class)
@Import({
        FtgoSecurityFilterChainConfig.class,
        FtgoCorsConfig.class,
        SecurityExceptionHandler.class
})
public class FtgoSecurityAutoConfiguration {
    // Auto-configuration entry point - imports all security components
}
