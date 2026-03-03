package net.chrisrichardson.ftgo.authorization.util;

import net.chrisrichardson.ftgo.authorization.model.FtgoPermission;
import net.chrisrichardson.ftgo.authorization.model.FtgoRole;
import net.chrisrichardson.ftgo.authorization.model.RolePermissionMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class providing convenient methods for authorization checks.
 *
 * <p>This class can be used in service code to perform programmatic
 * authorization checks beyond what annotations provide. It reads from
 * the current Spring Security context.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * if (AuthorizationUtils.isAdmin()) {
 *     // Show admin panel
 * }
 *
 * if (AuthorizationUtils.hasPermission(FtgoPermission.ORDER_CREATE)) {
 *     // Allow order creation
 * }
 * </pre>
 */
public final class AuthorizationUtils {

    private AuthorizationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns the current authentication, or null if not authenticated.
     *
     * @return the current authentication
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Returns the authenticated user's ID (principal name).
     *
     * @return the user ID, or null if not authenticated
     */
    public static String getCurrentUserId() {
        Authentication auth = getCurrentAuthentication();
        return auth != null ? auth.getName() : null;
    }

    /**
     * Checks if the current user has the ADMIN role.
     *
     * @return true if the current user is an admin
     */
    public static boolean isAdmin() {
        return hasRole(FtgoRole.ADMIN);
    }

    /**
     * Checks if the current user has the specified role.
     *
     * @param role the role to check
     * @return true if the current user has the role
     */
    public static boolean hasRole(FtgoRole role) {
        Authentication auth = getCurrentAuthentication();
        if (auth == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String authStr = authority.getAuthority();
            if (role.getAuthority().equals(authStr) || role.getRoleName().equals(authStr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the FTGO roles for the current user.
     *
     * @return a list of roles, empty if not authenticated
     */
    public static List<FtgoRole> getCurrentRoles() {
        Authentication auth = getCurrentAuthentication();
        if (auth == null) {
            return Collections.emptyList();
        }
        List<FtgoRole> roles = new ArrayList<>();
        for (GrantedAuthority authority : auth.getAuthorities()) {
            try {
                roles.add(FtgoRole.fromString(authority.getAuthority()));
            } catch (IllegalArgumentException e) {
                // Not an FtgoRole, skip
            }
        }
        return roles;
    }

    /**
     * Checks if the current user has the specified permission.
     *
     * @param permission the permission to check
     * @return true if the current user has the permission
     */
    public static boolean hasPermission(FtgoPermission permission) {
        Authentication auth = getCurrentAuthentication();
        if (auth == null) {
            return false;
        }

        // Admin has all permissions
        if (isAdmin()) {
            return true;
        }

        Set<FtgoPermission> userPermissions = resolvePermissions(auth);
        return userPermissions.contains(permission);
    }

    /**
     * Resolves all permissions for the given authentication based on roles.
     */
    private static Set<FtgoPermission> resolvePermissions(Authentication auth) {
        EnumSet<FtgoPermission> permissions = EnumSet.noneOf(FtgoPermission.class);
        for (GrantedAuthority authority : auth.getAuthorities()) {
            try {
                FtgoRole role = FtgoRole.fromString(authority.getAuthority());
                permissions.addAll(RolePermissionMapping.getPermissionsForRole(role));
            } catch (IllegalArgumentException e) {
                // Not an FtgoRole, skip
            }
        }
        return permissions;
    }
}
