package com.ftgo.security.authorization;

import com.ftgo.security.jwt.FtgoUserDetails;
import com.ftgo.security.jwt.JwtAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom {@link PermissionEvaluator} for FTGO resource ownership validation.
 *
 * <p>This evaluator is used with Spring Security's {@code hasPermission()} expressions
 * in {@code @PreAuthorize} annotations to enforce resource-level access control.
 *
 * <p>It supports two modes:
 * <ul>
 *   <li><strong>Object-based:</strong> {@code hasPermission(targetObject, permission)}
 *       — validates access to a specific domain object</li>
 *   <li><strong>ID-based:</strong> {@code hasPermission(targetId, targetType, permission)}
 *       — validates access by resource ID and type without loading the object</li>
 * </ul>
 *
 * <h3>Resource Ownership</h3>
 * <p>The evaluator checks if the authenticated user owns the target resource by
 * comparing the user's ID (from JWT {@code sub} claim) with the resource's owner ID.
 * ADMIN users bypass ownership checks and have access to all resources.
 *
 * <h3>Usage Examples</h3>
 * <pre>
 * // Check ownership of a loaded object
 * &#064;PreAuthorize("hasPermission(#consumer, 'VIEW')")
 * public Consumer getConsumer(Consumer consumer) { ... }
 *
 * // Check ownership by resource ID and type
 * &#064;PreAuthorize("hasPermission(#consumerId, 'Consumer', 'VIEW')")
 * public Consumer getConsumer(Long consumerId) { ... }
 * </pre>
 *
 * @see ResourceOwner
 */
@Component
public class FtgoPermissionEvaluator implements PermissionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(FtgoPermissionEvaluator.class);

    /**
     * Evaluates permission for a target domain object.
     *
     * <p>If the target implements {@link ResourceOwner}, the evaluator checks
     * whether the authenticated user's ID matches the resource owner's ID.
     * ADMIN users always have access.
     *
     * @param authentication the current authentication
     * @param targetDomainObject the domain object to check access for
     * @param permission the permission to check (e.g., "VIEW", "UPDATE")
     * @return {@code true} if access is granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null) {
            return false;
        }

        // ADMIN role bypasses all permission checks
        if (isAdmin(authentication)) {
            log.debug("ADMIN access granted for permission '{}' on {}", permission,
                    targetDomainObject.getClass().getSimpleName());
            return true;
        }

        // Check resource ownership if the target implements ResourceOwner
        if (targetDomainObject instanceof ResourceOwner) {
            ResourceOwner resource = (ResourceOwner) targetDomainObject;
            String currentUserId = extractUserId(authentication);
            if (currentUserId == null) {
                log.debug("Permission denied: no user ID in authentication");
                return false;
            }

            boolean isOwner = currentUserId.equals(resource.getOwnerId());
            if (!isOwner) {
                log.debug("Permission '{}' denied for user '{}' on {} (owner: '{}')",
                        permission, currentUserId, targetDomainObject.getClass().getSimpleName(),
                        resource.getOwnerId());
            }
            return isOwner;
        }

        log.debug("Permission '{}' denied: target {} does not implement ResourceOwner",
                permission, targetDomainObject.getClass().getSimpleName());
        return false;
    }

    /**
     * Evaluates permission for a target identified by ID and type.
     *
     * <p>This method is used when the target object is not yet loaded.
     * It checks if the target ID matches the current user's ID (ownership by convention).
     * ADMIN users always have access.
     *
     * <p>The convention assumes that the target ID is the owner's user ID when
     * validating ownership. For more complex ownership models, services should
     * override this evaluator or load the resource and use the object-based method.
     *
     * @param authentication the current authentication
     * @param targetId the ID of the target resource
     * @param targetType the type of the target resource (e.g., "Consumer", "Order")
     * @param permission the permission to check
     * @return {@code true} if access is granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {
        if (authentication == null || targetId == null) {
            return false;
        }

        // ADMIN role bypasses all permission checks
        if (isAdmin(authentication)) {
            log.debug("ADMIN access granted for permission '{}' on {}#{}", permission, targetType, targetId);
            return true;
        }

        // Check if the target ID matches the current user's ID
        String currentUserId = extractUserId(authentication);
        if (currentUserId == null) {
            log.debug("Permission denied: no user ID in authentication");
            return false;
        }

        boolean isOwner = currentUserId.equals(String.valueOf(targetId));
        if (!isOwner) {
            log.debug("Permission '{}' denied for user '{}' on {}#{}", permission,
                    currentUserId, targetType, targetId);
        }
        return isOwner;
    }

    /**
     * Checks if the authenticated user has the ADMIN role.
     *
     * @param authentication the current authentication
     * @return {@code true} if the user has ROLE_ADMIN
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> FtgoRole.ADMIN.getAuthority().equals(a.getAuthority()));
    }

    /**
     * Extracts the user ID from the authentication.
     *
     * @param authentication the current authentication
     * @return the user ID, or {@code null} if not available
     */
    private String extractUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            FtgoUserDetails userDetails = ((JwtAuthenticationToken) authentication).getUserDetails();
            return userDetails.getUserId();
        }
        return authentication.getName();
    }
}
