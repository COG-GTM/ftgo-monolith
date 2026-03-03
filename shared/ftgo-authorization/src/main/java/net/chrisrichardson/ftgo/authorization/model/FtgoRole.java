package net.chrisrichardson.ftgo.authorization.model;

/**
 * Defines the roles available in the FTGO system.
 *
 * <p>Roles are hierarchical, with ADMIN at the top inheriting all permissions
 * from subordinate roles. The hierarchy is:</p>
 * <pre>
 * ADMIN &gt; RESTAURANT_OWNER &gt; COURIER &gt; CUSTOMER
 * </pre>
 *
 * <p>Roles are stored in JWT claims and mapped to Spring Security
 * GrantedAuthority instances with the {@code ROLE_} prefix.</p>
 *
 * @see FtgoPermission
 */
public enum FtgoRole {

    /**
     * End consumer who places orders and tracks deliveries.
     * Base role in the hierarchy.
     */
    CUSTOMER("ROLE_CUSTOMER", "End consumer who places orders"),

    /**
     * Restaurant owner who manages restaurants, menus, and accepts orders.
     */
    RESTAURANT_OWNER("ROLE_RESTAURANT_OWNER", "Restaurant owner/manager"),

    /**
     * Courier who delivers orders and updates delivery status.
     */
    COURIER("ROLE_COURIER", "Delivery courier"),

    /**
     * System administrator with full access to all resources.
     * Top of the role hierarchy.
     */
    ADMIN("ROLE_ADMIN", "System administrator with full access");

    private final String authority;
    private final String description;

    FtgoRole(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }

    /**
     * Returns the Spring Security authority string (e.g., ROLE_CUSTOMER).
     *
     * @return the authority string with ROLE_ prefix
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Returns a human-readable description of the role.
     *
     * @return the role description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the role name without the ROLE_ prefix.
     *
     * @return the simple role name (e.g., CUSTOMER)
     */
    public String getRoleName() {
        return name();
    }

    /**
     * Finds an FtgoRole by its name, supporting both prefixed and unprefixed forms.
     *
     * @param roleName the role name (e.g., "CUSTOMER" or "ROLE_CUSTOMER")
     * @return the matching FtgoRole
     * @throws IllegalArgumentException if no matching role is found
     */
    public static FtgoRole fromString(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }
        String normalized = roleName.startsWith("ROLE_")
                ? roleName.substring(5)
                : roleName;
        return valueOf(normalized.toUpperCase());
    }
}
