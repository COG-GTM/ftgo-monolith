package com.ftgo.security.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Configures the FTGO role hierarchy.
 * <p>
 * The hierarchy defines which roles inherit permissions from other roles:
 * <pre>
 * ROLE_ADMIN > ROLE_RESTAURANT_OWNER
 * ROLE_ADMIN > ROLE_COURIER
 * ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER
 * </pre>
 * This means:
 * <ul>
 *   <li>{@code ADMIN} inherits all permissions from all other roles</li>
 *   <li>{@code RESTAURANT_OWNER} inherits all {@code CUSTOMER} permissions</li>
 *   <li>{@code COURIER} does not inherit from other non-admin roles</li>
 *   <li>{@code CUSTOMER} is the base role with no inheritance</li>
 * </ul>
 * </p>
 *
 * @see FtgoRole
 */
@Configuration
public class FtgoRoleHierarchyConfiguration {

    /**
     * Role hierarchy definition string.
     * <p>
     * Format: {@code ROLE_HIGHER > ROLE_LOWER} (one per line).
     * </p>
     */
    public static final String ROLE_HIERARCHY_DEFINITION =
            "ROLE_ADMIN > ROLE_RESTAURANT_OWNER\n"
            + "ROLE_ADMIN > ROLE_COURIER\n"
            + "ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER";

    /**
     * Creates a {@link RoleHierarchy} bean that Spring Security uses
     * when evaluating role-based access decisions.
     *
     * @return configured role hierarchy
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(ROLE_HIERARCHY_DEFINITION);
        return hierarchy;
    }
}
