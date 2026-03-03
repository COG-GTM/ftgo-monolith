package net.chrisrichardson.ftgo.authorization.config;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Configures the FTGO role hierarchy.
 *
 * <p>The role hierarchy defines inheritance relationships between roles.
 * Higher roles automatically inherit all permissions of lower roles.
 * The hierarchy is:</p>
 * <pre>
 * ROLE_ADMIN &gt; ROLE_RESTAURANT_OWNER
 * ROLE_ADMIN &gt; ROLE_COURIER
 * ROLE_ADMIN &gt; ROLE_CUSTOMER
 * ROLE_RESTAURANT_OWNER &gt; ROLE_CUSTOMER
 * ROLE_COURIER &gt; ROLE_CUSTOMER
 * </pre>
 *
 * <p>This means:</p>
 * <ul>
 *   <li>ADMIN inherits all permissions from all other roles</li>
 *   <li>RESTAURANT_OWNER inherits CUSTOMER permissions</li>
 *   <li>COURIER inherits CUSTOMER permissions</li>
 *   <li>CUSTOMER is the base role with no inheritance</li>
 * </ul>
 *
 * @see org.springframework.security.access.hierarchicalroles.RoleHierarchy
 */
public class FtgoRoleHierarchyConfig {

    /**
     * Role hierarchy definition string using Spring Security's format.
     * Each line defines a "parent &gt; child" relationship.
     */
    public static final String ROLE_HIERARCHY_STRING =
            "ROLE_ADMIN > ROLE_RESTAURANT_OWNER\n" +
            "ROLE_ADMIN > ROLE_COURIER\n" +
            "ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER\n" +
            "ROLE_COURIER > ROLE_CUSTOMER";

    private FtgoRoleHierarchyConfig() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates and returns a configured {@link RoleHierarchy} bean.
     *
     * @return the role hierarchy with FTGO role relationships
     */
    public static RoleHierarchy createRoleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(ROLE_HIERARCHY_STRING);
        return roleHierarchy;
    }
}
