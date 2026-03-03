package net.chrisrichardson.ftgo.authorization.evaluator;

import net.chrisrichardson.ftgo.authorization.model.FtgoPermission;
import net.chrisrichardson.ftgo.authorization.model.FtgoRole;
import net.chrisrichardson.ftgo.authorization.model.RolePermissionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Custom Spring Security {@link PermissionEvaluator} for the FTGO authorization framework.
 *
 * <p>This evaluator handles two types of permission checks:</p>
 * <ol>
 *   <li><strong>Permission-based</strong>: Checks if the user's roles grant the
 *       required permission (e.g., {@code hasPermission(authentication, null, 'order:create')})</li>
 *   <li><strong>Resource ownership</strong>: Checks if the user owns the target resource
 *       (e.g., {@code hasPermission(authentication, orderId, 'order', 'read')})</li>
 * </ol>
 *
 * <p>ADMIN users automatically pass all permission checks due to role hierarchy.</p>
 *
 * @see ResourceOwnershipResolver
 * @see RolePermissionMapping
 */
public class FtgoPermissionEvaluator implements PermissionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(FtgoPermissionEvaluator.class);

    private final List<ResourceOwnershipResolver> ownershipResolvers;

    public FtgoPermissionEvaluator(List<ResourceOwnershipResolver> ownershipResolvers) {
        this.ownershipResolvers = ownershipResolvers != null
                ? ownershipResolvers
                : Collections.<ResourceOwnershipResolver>emptyList();
    }

    /**
     * Evaluates whether the user has the specified permission.
     *
     * <p>The permission parameter should be a permission string in the format
     * {@code service:action} (e.g., "order:create", "restaurant:read").</p>
     *
     * @param authentication the current authentication
     * @param targetDomainObject not used for simple permission checks (can be null)
     * @param permission the permission string to evaluate
     * @return true if the user has the required permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject,
                                  Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        String permissionStr = permission.toString();
        String userId = authentication.getName();

        // ADMIN role bypasses all permission checks
        if (hasAdminRole(authentication)) {
            log.debug("Admin user '{}' granted permission '{}'", userId, permissionStr);
            return true;
        }

        // Resolve user roles and check permission mapping
        Set<FtgoPermission> userPermissions = resolveUserPermissions(authentication);
        try {
            FtgoPermission required = FtgoPermission.fromValue(permissionStr);
            boolean hasPermission = userPermissions.contains(required);
            log.debug("User '{}' permission check for '{}': {}", userId, permissionStr, hasPermission);
            return hasPermission;
        } catch (IllegalArgumentException e) {
            log.warn("Unknown permission '{}' requested by user '{}'", permissionStr, userId);
            return false;
        }
    }

    /**
     * Evaluates whether the user has permission to access a specific resource.
     *
     * <p>This method performs both a permission check and a resource ownership check.
     * The user must have the required permission AND either be the resource owner
     * or have the ADMIN role.</p>
     *
     * @param authentication the current authentication
     * @param targetId the resource identifier
     * @param targetType the resource type (e.g., "order", "consumer")
     * @param permission the permission string (e.g., "read", "update")
     * @return true if the user has permission to access the resource
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                  String targetType, Object permission) {
        if (authentication == null || targetType == null || permission == null) {
            return false;
        }

        String userId = authentication.getName();
        String permissionStr = targetType + ":" + permission.toString();

        // ADMIN role bypasses all permission and ownership checks
        if (hasAdminRole(authentication)) {
            log.debug("Admin user '{}' granted access to {} #{}", userId, targetType, targetId);
            return true;
        }

        // First check if the user has the permission
        Set<FtgoPermission> userPermissions = resolveUserPermissions(authentication);
        try {
            FtgoPermission required = FtgoPermission.fromValue(permissionStr);
            if (!userPermissions.contains(required)) {
                log.debug("User '{}' denied access to {} #{}: missing permission '{}'",
                        userId, targetType, targetId, permissionStr);
                return false;
            }
        } catch (IllegalArgumentException e) {
            log.warn("Unknown permission '{}' for resource {} #{}", permissionStr, targetType, targetId);
            return false;
        }

        // Then check resource ownership
        if (targetId != null) {
            boolean isOwner = checkResourceOwnership(userId, targetId, targetType);
            log.debug("User '{}' ownership check for {} #{}: {}",
                    userId, targetType, targetId, isOwner);
            return isOwner;
        }

        return true;
    }

    /**
     * Checks if the authenticated user has the ADMIN role.
     */
    private boolean hasAdminRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            if (FtgoRole.ADMIN.getAuthority().equals(auth) || "ADMIN".equals(auth)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves all permissions for the user based on their roles.
     */
    private Set<FtgoPermission> resolveUserPermissions(Authentication authentication) {
        EnumSet<FtgoPermission> permissions = EnumSet.noneOf(FtgoPermission.class);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            try {
                FtgoRole role = FtgoRole.fromString(authority.getAuthority());
                permissions.addAll(RolePermissionMapping.getPermissionsForRole(role));
            } catch (IllegalArgumentException e) {
                // Not an FtgoRole, skip
                log.trace("Skipping non-FTGO authority: {}", authority.getAuthority());
            }
        }

        return permissions;
    }

    /**
     * Delegates resource ownership check to the appropriate resolver.
     */
    private boolean checkResourceOwnership(String userId, Serializable resourceId,
                                            String resourceType) {
        for (ResourceOwnershipResolver resolver : ownershipResolvers) {
            if (resolver.supports(resourceType)) {
                return resolver.isOwner(userId, resourceId);
            }
        }
        // If no resolver is registered for this resource type,
        // deny access by default (secure by default)
        log.warn("No ownership resolver found for resource type '{}'. Access denied.", resourceType);
        return false;
    }
}
