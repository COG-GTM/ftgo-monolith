package net.chrisrichardson.ftgo.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO security.
 *
 * Importing this configuration (or relying on Spring Boot auto-configuration)
 * activates the base security filter chain, CORS filter, and actuator security.
 */
@Configuration
@Import({
        FtgoWebSecurityConfiguration.class,
        FtgoCorsConfiguration.class,
        FtgoActuatorSecurityConfiguration.class
})
public class FtgoSecurityAutoConfiguration {
}
