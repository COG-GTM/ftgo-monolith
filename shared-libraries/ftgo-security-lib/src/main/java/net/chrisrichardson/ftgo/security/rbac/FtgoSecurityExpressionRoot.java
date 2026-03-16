package net.chrisrichardson.ftgo.security.rbac;

import net.chrisrichardson.ftgo.security.jwt.JwtAuthenticationToken;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/**
 * Custom Spring Security expression root that provides FTGO-specific
 * SpEL functions for use in {@code @PreAuthorize} and {@code @PostAuthorize}
 * annotations.
 *
 * <p>In addition to all standard Spring Security expressions ({@code hasRole},
 * {@code hasAuthority}, {@code isAuthenticated}, etc.), this root adds:
 *
 * <ul>
 *   <li>{@code isResourceOwner(resourceOwnerId)} — checks whether the
 *       authenticated user's ID matches the given resource owner ID</li>
 *   <li>{@code hasPermission('order:create')} — checks whether the
 *       authenticated user holds a specific FTGO permission (delegates to
 *       {@code hasAuthority})</li>
 *   <li>{@code isAdmin()} — shorthand for checking the ADMIN role</li>
 *   <li>{@code isResourceOwnerOrAdmin(resourceOwnerId)} — convenience
 *       combination of ownership and admin checks</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * &#64;PreAuthorize("isResourceOwner(#consumerId) or isAdmin()")
 * public Consumer getConsumer(long consumerId) { ... }
 * </pre>
 *
 * @see FtgoMethodSecurityExpressionHandler
 */
public class FtgoSecurityExpressionRoot extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;

    public FtgoSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    /**
     * Returns {@code true} if the currently authenticated user's ID matches
     * the given {@code resourceOwnerId}.
     *
     * <p>This enables ownership-based access control without hard-coding
     * user-ID comparisons in service code:
     * <pre>
     * &#64;PreAuthorize("isResourceOwner(#orderId)")
     * </pre>
     *
     * @param resourceOwnerId the owner identifier of the resource being accessed
     * @return {@code true} if the current user owns the resource
     */
    public boolean isResourceOwner(String resourceOwnerId) {
        Authentication auth = getAuthentication();
        if (auth instanceof JwtAuthenticationToken) {
            String userId = ((JwtAuthenticationToken) auth).getUserId();
            return userId != null && userId.equals(resourceOwnerId);
        }
        return false;
    }

    /**
     * Returns {@code true} if the currently authenticated user has the ADMIN role.
     */
    public boolean isAdmin() {
        Authentication auth = getAuthentication();
        if (auth instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) auth).getRoles()
                    .contains(Role.ADMIN.getAuthority());
        }
        return hasRole("ADMIN");
    }

    /**
     * Returns {@code true} if the current user is either the resource owner
     * or an administrator.
     *
     * @param resourceOwnerId the owner identifier of the resource being accessed
     * @return {@code true} if the user owns the resource or is an admin
     */
    public boolean isResourceOwnerOrAdmin(String resourceOwnerId) {
        return isResourceOwner(resourceOwnerId) || isAdmin();
    }

    /**
     * Returns {@code true} if the current user holds the specified FTGO permission.
     *
     * <p>This delegates to {@link #hasAuthority(String)} but provides a more
     * intention-revealing name in SpEL expressions:
     * <pre>
     * &#64;PreAuthorize("hasFtgoPermission('order:create')")
     * </pre>
     *
     * @param permission the permission authority string (e.g. {@code "order:create"})
     * @return {@code true} if the user has the permission
     */
    public boolean hasFtgoPermission(String permission) {
        return hasAuthority(permission);
    }

    /**
     * Returns {@code true} if the current user holds the specified {@link Role}.
     *
     * @param role the FTGO role to check
     * @return {@code true} if the user has the role
     */
    public boolean hasFtgoRole(Role role) {
        return hasAuthority(role.getAuthority());
    }

    // -- MethodSecurityExpressionOperations contract --

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }
}
