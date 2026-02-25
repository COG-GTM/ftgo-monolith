package com.ftgo.security.authorization;

/**
 * Defines the roles available in the FTGO platform.
 * <p>
 * Each role maps to a bounded context's business rules and determines
 * what operations a user can perform across the platform services.
 * </p>
 *
 * <h3>Role Hierarchy</h3>
 * <pre>
 * ADMIN
 *   ├── RESTAURANT_OWNER
 *   │     └── CUSTOMER
 *   └── COURIER
 * </pre>
 * <p>
 * {@code ADMIN} inherits all permissions from all other roles.
 * {@code RESTAURANT_OWNER} inherits {@code CUSTOMER} permissions.
 * </p>
 *
 * @see FtgoPermission
 * @see FtgoRoleHierarchyConfiguration
 */
public enum FtgoRole {

    /**
     * Customer role — can create/view own orders, view restaurants/menus,
     * track own deliveries.
     */
    CUSTOMER("ROLE_CUSTOMER"),

    /**
     * Restaurant owner role — can manage own restaurant, view related orders.
     * Inherits CUSTOMER permissions.
     */
    RESTAURANT_OWNER("ROLE_RESTAURANT_OWNER"),

    /**
     * Courier role — can view assigned orders and update delivery status.
     */
    COURIER("ROLE_COURIER"),

    /**
     * Administrator role — full access to all operations across all services.
     * Inherits all other role permissions.
     */
    ADMIN("ROLE_ADMIN");

    private final String authority;

    FtgoRole(String authority) {
        this.authority = authority;
    }

    /**
     * Returns the Spring Security authority string (e.g. {@code ROLE_ADMIN}).
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Returns the role name without the {@code ROLE_} prefix.
     */
    public String getRoleName() {
        return name();
    }

    /**
     * Resolves a {@link FtgoRole} from a Spring Security authority string.
     *
     * @param authority the authority string (e.g. {@code ROLE_ADMIN})
     * @return the matching role
     * @throws IllegalArgumentException if no matching role is found
     */
    public static FtgoRole fromAuthority(String authority) {
        for (FtgoRole role : values()) {
            if (role.authority.equals(authority)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role authority: " + authority);
    }
}
