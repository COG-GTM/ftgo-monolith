package com.ftgo.security.authorization;

import com.ftgo.security.jwt.JwtUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom {@link PermissionEvaluator} for the FTGO platform that supports
 * both permission-based and resource-ownership-based authorization.
 *
 * <h3>Permission-based evaluation</h3>
 * <p>
 * Checks if the authenticated user's JWT permissions claim contains the
 * requested permission string. Used with:
 * <pre>
 * &#64;PreAuthorize("hasPermission(null, 'order:create')")
 * </pre>
 * </p>
 *
 * <h3>Resource ownership evaluation</h3>
 * <p>
 * Validates that a user owns the resource they are trying to access.
 * Services register {@link ResourceOwnershipStrategy} implementations
 * to define how ownership is determined for each resource type.
 * Used with:
 * <pre>
 * &#64;PreAuthorize("hasPermission(#orderId, 'Order', 'VIEW')")
 * </pre>
 * </p>
 *
 * @see ResourceOwnershipStrategy
 * @see FtgoMethodSecurityConfiguration
 */
@Component
public class FtgoPermissionEvaluator implements PermissionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(FtgoPermissionEvaluator.class);

    private final Map<String, ResourceOwnershipStrategy> ownershipStrategies =
            new ConcurrentHashMap<>();

    /**
     * Registers a {@link ResourceOwnershipStrategy} for a specific resource type.
     *
     * @param resourceType the resource type name (e.g. "Order", "Restaurant")
     * @param strategy     the ownership strategy
     */
    public void registerOwnershipStrategy(String resourceType,
                                          ResourceOwnershipStrategy strategy) {
        ownershipStrategies.put(resourceType.toUpperCase(), strategy);
        log.info("Registered ownership strategy for resource type: {}", resourceType);
    }

    /**
     * Evaluates permission using the target domain object.
     * <p>
     * If the {@code permission} is a simple string (e.g. {@code "order:create"}),
     * checks the user's JWT permissions claim. ADMIN role always passes.
     * </p>
     *
     * @param authentication the current authentication
     * @param targetDomainObject the target resource (can be null for simple permission checks)
     * @param permission the permission to check
     * @return true if access is granted
     */
    @Override
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomainObject,
                                 Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        String permissionString = permission.toString();

        // ADMIN always has access
        if (isAdmin(authentication)) {
            log.debug("ADMIN access granted for permission '{}'", permissionString);
            return true;
        }

        // Check JWT permissions claim
        if (authentication.getPrincipal() instanceof JwtUserDetails userDetails) {
            List<String> userPermissions = userDetails.getPermissions();
            boolean hasPermission = userPermissions.contains(permissionString);
            log.debug("Permission check '{}' for user '{}': {}",
                    permissionString, userDetails.getUsername(), hasPermission);
            return hasPermission;
        }

        return false;
    }

    /**
     * Evaluates permission using the target resource ID and type.
     * <p>
     * First checks if the user has the required permission, then
     * validates resource ownership if a {@link ResourceOwnershipStrategy}
     * is registered for the target type.
     * </p>
     *
     * @param authentication the current authentication
     * @param targetId the ID of the target resource
     * @param targetType the type of the target resource (e.g. "Order")
     * @param permission the permission/operation to check (e.g. "VIEW")
     * @return true if access is granted
     */
    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        if (authentication == null || targetType == null || permission == null) {
            return false;
        }

        // ADMIN always has access
        if (isAdmin(authentication)) {
            log.debug("ADMIN access granted for {} {} with permission '{}'",
                    targetType, targetId, permission);
            return true;
        }

        // Build the permission string: e.g. "order:view"
        String permissionString = targetType.toLowerCase() + ":"
                + permission.toString().toLowerCase();

        // Check JWT permissions claim
        if (!(authentication.getPrincipal() instanceof JwtUserDetails userDetails)) {
            return false;
        }

        boolean hasPermission = userDetails.getPermissions().contains(permissionString);
        if (!hasPermission) {
            log.debug("Permission '{}' denied for user '{}'",
                    permissionString, userDetails.getUsername());
            return false;
        }

        // If an ownership strategy is registered, also validate ownership
        ResourceOwnershipStrategy strategy =
                ownershipStrategies.get(targetType.toUpperCase());
        if (strategy != null && targetId != null) {
            boolean isOwner = strategy.isOwner(userDetails.getUserId(), targetId);
            log.debug("Ownership check for {} {} by user {}: {}",
                    targetType, targetId, userDetails.getUserId(), isOwner);
            return isOwner;
        }

        return true;
    }

    /**
     * Checks if the authenticated user has the ADMIN role.
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> FtgoRole.ADMIN.getAuthority().equals(a.getAuthority()));
    }
}
