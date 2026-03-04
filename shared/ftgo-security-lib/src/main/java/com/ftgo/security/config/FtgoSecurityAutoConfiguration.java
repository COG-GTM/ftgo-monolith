package com.ftgo.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO Security.
 *
 * <p>Imports all security-related configuration classes so that consuming
 * microservices only need to add this library as a dependency and
 * optionally override properties.
 */
@Configuration
@Import({
        FtgoBaseSecurityConfig.class,
        FtgoCorsConfig.class,
        FtgoActuatorSecurityConfig.class
})
public class FtgoSecurityAutoConfiguration {
}
