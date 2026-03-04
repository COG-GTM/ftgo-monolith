package com.ftgo.security.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Configures the FTGO role hierarchy for Spring Security.
 *
 * <p>The role hierarchy defines inheritance relationships between roles:
 * <pre>
 * ROLE_ADMIN &gt; ROLE_RESTAURANT_OWNER
 * ROLE_RESTAURANT_OWNER &gt; ROLE_CUSTOMER
 * ROLE_ADMIN &gt; ROLE_COURIER
 * </pre>
 *
 * <p>This means:
 * <ul>
 *   <li>ADMIN inherits all permissions of RESTAURANT_OWNER, CUSTOMER, and COURIER</li>
 *   <li>RESTAURANT_OWNER inherits all permissions of CUSTOMER</li>
 *   <li>COURIER has its own permissions (no inheritance from/to CUSTOMER or RESTAURANT_OWNER)</li>
 * </ul>
 *
 * <p>The hierarchy is used by Spring Security's {@code hasRole()} and
 * {@code hasAuthority()} expressions in {@code @PreAuthorize} annotations.
 */
@Configuration
public class FtgoRoleHierarchyConfig {

    private static final Logger log = LoggerFactory.getLogger(FtgoRoleHierarchyConfig.class);

    /**
     * Role hierarchy definition string.
     *
     * <p>Each line defines a "parent &gt; child" relationship where the parent
     * role inherits all authorities of the child role.
     */
    static final String ROLE_HIERARCHY_DEFINITION =
            "ROLE_ADMIN > ROLE_RESTAURANT_OWNER\n"
                    + "ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER\n"
                    + "ROLE_ADMIN > ROLE_COURIER";

    /**
     * Creates the role hierarchy bean.
     *
     * @return the configured role hierarchy
     */
    @Bean
    @ConditionalOnMissingBean(RoleHierarchy.class)
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(ROLE_HIERARCHY_DEFINITION);
        log.info("Configured FTGO role hierarchy: ADMIN > RESTAURANT_OWNER > CUSTOMER, ADMIN > COURIER");
        return hierarchy;
    }

    /**
     * Creates a method security expression handler that is aware of the role hierarchy.
     *
     * <p>This ensures that {@code @PreAuthorize} expressions like {@code hasRole('CUSTOMER')}
     * correctly account for the role hierarchy (e.g., ADMIN users pass CUSTOMER checks).
     *
     * @param roleHierarchy the role hierarchy
     * @param permissionEvaluator the custom permission evaluator
     * @return the configured expression handler
     */
    @Bean
    @ConditionalOnMissingBean(MethodSecurityExpressionHandler.class)
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy,
            FtgoPermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
