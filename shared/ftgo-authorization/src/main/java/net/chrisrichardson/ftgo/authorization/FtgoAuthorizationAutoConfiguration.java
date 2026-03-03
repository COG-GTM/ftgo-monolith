package net.chrisrichardson.ftgo.authorization;

import net.chrisrichardson.ftgo.authorization.config.FtgoMethodSecurityConfig;
import net.chrisrichardson.ftgo.authorization.config.FtgoRoleHierarchyConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for FTGO Authorization.
 *
 * <p>This is the main entry point for the FTGO authorization library. When included
 * as a dependency, it automatically configures:</p>
 * <ul>
 *   <li>Method-level security with {@code @PreAuthorize} / {@code @PostAuthorize} support</li>
 *   <li>FTGO role hierarchy (ADMIN &gt; RESTAURANT_OWNER &gt; COURIER &gt; CUSTOMER)</li>
 *   <li>Custom permission evaluator with resource ownership validation</li>
 *   <li>Role-to-permission mapping for all FTGO bounded contexts</li>
 * </ul>
 *
 * <p>To use this library:</p>
 * <ol>
 *   <li>Add {@code ftgo-authorization} as a dependency</li>
 *   <li>Optionally register {@link net.chrisrichardson.ftgo.authorization.evaluator.ResourceOwnershipResolver}
 *       beans for resource ownership validation</li>
 *   <li>Use {@code @PreAuthorize} annotations on service/controller methods</li>
 * </ol>
 *
 * <p>This configuration is only active for servlet-based web applications.</p>
 *
 * @see FtgoMethodSecurityConfig
 * @see FtgoRoleHierarchyConfig
 */
@Configuration
@ConditionalOnWebApplication
@Import({
        FtgoMethodSecurityConfig.class
})
public class FtgoAuthorizationAutoConfiguration {
    // Auto-configuration entry point - imports all authorization components
}
