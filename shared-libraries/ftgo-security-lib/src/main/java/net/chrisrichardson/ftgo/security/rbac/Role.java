package net.chrisrichardson.ftgo.security.rbac;

import net.chrisrichardson.ftgo.security.util.SecurityConstants;

/**
 * Enumerates the application-level roles recognised by the FTGO platform.
 *
 * <p>Each role maps to a Spring Security authority with the {@value SecurityConstants#ROLE_PREFIX}
 * prefix so that it can be used seamlessly with {@code hasRole()} / {@code hasAuthority()}
 * expressions in {@code @PreAuthorize} annotations.
 *
 * <p>Roles are coarse-grained and represent the user's primary identity within the system.
 * Fine-grained access control is handled by {@link Permission} values that are mapped to
 * roles via {@link RolePermissionMapping}.
 */
public enum Role {

    /** A consumer who places food orders. */
    CUSTOMER("ROLE_CUSTOMER"),

    /** A restaurant owner who manages restaurants and menus. */
    RESTAURANT_OWNER("ROLE_RESTAURANT_OWNER"),

    /** A courier who delivers orders. */
    COURIER("ROLE_COURIER"),

    /** A platform administrator with full access. */
    ADMIN("ROLE_ADMIN");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    /**
     * Returns the Spring Security authority string for this role
     * (e.g.&nbsp;{@code ROLE_CUSTOMER}).
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Resolves a {@link Role} from its authority string.
     *
     * @param authority the authority string (e.g. {@code "ROLE_ADMIN"})
     * @return the matching {@link Role}
     * @throws IllegalArgumentException if no role matches
     */
    public static Role fromAuthority(String authority) {
        for (Role role : values()) {
            if (role.authority.equals(authority)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role authority: " + authority);
    }
}
