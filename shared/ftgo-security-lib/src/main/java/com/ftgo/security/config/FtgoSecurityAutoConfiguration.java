package com.ftgo.security.config;

import com.ftgo.security.authorization.FtgoRoleHierarchyConfig;
import com.ftgo.security.jwt.JwtAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO Security.
 *
 * <p>Imports all security-related configuration classes so that consuming
 * microservices only need to add this library as a dependency and
 * optionally override properties. Includes JWT authentication support
 * via {@link JwtAutoConfiguration} and role-based authorization via
 * {@link FtgoRoleHierarchyConfig}.
 */
@Configuration
@Import({
        FtgoBaseSecurityConfig.class,
        FtgoCorsConfig.class,
        FtgoActuatorSecurityConfig.class,
        JwtAutoConfiguration.class,
        FtgoRoleHierarchyConfig.class
})
public class FtgoSecurityAutoConfiguration {
}
