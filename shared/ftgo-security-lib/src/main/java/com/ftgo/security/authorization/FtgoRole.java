package com.ftgo.security.authorization;

/**
 * Enumeration of all roles in the FTGO platform.
 *
 * <p>Roles are stored in JWT claims under the {@code roles} claim and are
 * mapped to Spring Security {@code ROLE_*} granted authorities by the
 * {@link com.ftgo.security.jwt.JwtAuthenticationFilter}.
 *
 * <h3>Role Hierarchy</h3>
 * <pre>
 * ADMIN &gt; RESTAURANT_OWNER &gt; CUSTOMER
 * ADMIN &gt; COURIER
 * </pre>
 *
 * <p>This means an ADMIN inherits all permissions of RESTAURANT_OWNER,
 * CUSTOMER, and COURIER. A RESTAURANT_OWNER inherits CUSTOMER permissions.
 *
 * @see FtgoPermission
 * @see FtgoRoleHierarchyConfig
 */
public enum FtgoRole {

    /**
     * End-user who places food orders.
     * <p>Permissions: Create/view own consumers, create/view/cancel own orders,
     * view restaurants, track own delivery.
     */
    CUSTOMER("ROLE_CUSTOMER"),

    /**
     * Owner of one or more restaurants on the platform.
     * <p>Permissions: Full CRUD on own restaurant, view related orders,
     * plus all CUSTOMER permissions (via hierarchy).
     */
    RESTAURANT_OWNER("ROLE_RESTAURANT_OWNER"),

    /**
     * Delivery courier who fulfills orders.
     * <p>Permissions: View assigned orders, update delivery status.
     */
    COURIER("ROLE_COURIER"),

    /**
     * Platform administrator with unrestricted access.
     * <p>Permissions: Full access to all services and resources.
     */
    ADMIN("ROLE_ADMIN");

    private final String authority;

    FtgoRole(String authority) {
        this.authority = authority;
    }

    /**
     * Returns the Spring Security authority string (e.g., {@code ROLE_CUSTOMER}).
     *
     * @return the authority string
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Returns the role name without the {@code ROLE_} prefix.
     *
     * @return the role name (e.g., {@code CUSTOMER})
     */
    public String getRoleName() {
        return name();
    }

    /**
     * Resolves a {@link FtgoRole} from a Spring Security authority string.
     *
     * @param authority the authority string (e.g., {@code ROLE_CUSTOMER} or {@code CUSTOMER})
     * @return the matching role
     * @throws IllegalArgumentException if no matching role is found
     */
    public static FtgoRole fromAuthority(String authority) {
        String normalized = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
        return valueOf(normalized);
    }
}
