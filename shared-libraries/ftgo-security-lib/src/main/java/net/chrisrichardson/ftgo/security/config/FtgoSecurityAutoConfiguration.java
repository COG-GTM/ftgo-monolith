package net.chrisrichardson.ftgo.security.config;

import net.chrisrichardson.ftgo.security.rbac.FtgoMethodSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO security.
 *
 * Importing this configuration (or relying on Spring Boot auto-configuration)
 * activates the base security filter chain, CORS filter, actuator security,
 * and method-level RBAC support.
 */
@Configuration
@Import({
        FtgoWebSecurityConfiguration.class,
        FtgoCorsConfiguration.class,
        FtgoActuatorSecurityConfiguration.class,
        FtgoMethodSecurityConfiguration.class
})
public class FtgoSecurityAutoConfiguration {
}
