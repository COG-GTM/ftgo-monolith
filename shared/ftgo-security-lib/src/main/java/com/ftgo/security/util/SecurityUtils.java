package com.ftgo.security.util;

import com.ftgo.security.jwt.JwtUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for common Spring Security operations.
 * <p>
 * Provides convenient static methods for accessing the current security context,
 * extracting user information, and checking authorities.
 * </p>
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns the current {@link Authentication} from the security context,
     * or {@link Optional#empty()} if no authentication is present.
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    /**
     * Returns the username of the currently authenticated user,
     * or {@link Optional#empty()} if not authenticated.
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentAuthentication().map(auth -> {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }
            return principal.toString();
        });
    }

    /**
     * Returns the authorities of the currently authenticated user,
     * or an empty collection if not authenticated.
     */
    public static Collection<? extends GrantedAuthority> getCurrentAuthorities() {
        return getCurrentAuthentication()
                .map(Authentication::getAuthorities)
                .orElse(Collections.emptyList());
    }

    /**
     * Checks if the current user has the specified authority (role).
     *
     * @param authority the authority to check (e.g., "ROLE_ADMIN")
     * @return true if the current user has the authority
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    /**
     * Checks if there is a currently authenticated user.
     *
     * @return true if a user is authenticated
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication().isPresent();
    }

    // ---------------------------------------------------------------
    // JWT-specific helpers (available when JWT auth is enabled)
    // ---------------------------------------------------------------

    /**
     * Returns the {@link JwtUserDetails} from the current security context,
     * or {@link Optional#empty()} if the principal is not JWT-based.
     */
    public static Optional<JwtUserDetails> getCurrentJwtUserDetails() {
        return getCurrentAuthentication()
                .map(Authentication::getPrincipal)
                .filter(JwtUserDetails.class::isInstance)
                .map(JwtUserDetails.class::cast);
    }

    /**
     * Returns the numeric user ID from the JWT claims,
     * or {@link Optional#empty()} if not authenticated via JWT.
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentJwtUserDetails().map(JwtUserDetails::getUserId);
    }

    /**
     * Returns the roles from the JWT claims,
     * or an empty list if not authenticated via JWT.
     */
    public static List<String> getCurrentRoles() {
        return getCurrentJwtUserDetails()
                .map(JwtUserDetails::getRoles)
                .orElse(Collections.emptyList());
    }

    /**
     * Returns the service-specific permissions from the JWT claims,
     * or an empty list if not authenticated via JWT.
     */
    public static List<String> getCurrentPermissions() {
        return getCurrentJwtUserDetails()
                .map(JwtUserDetails::getPermissions)
                .orElse(Collections.emptyList());
    }

    /**
     * Checks if the current JWT user has the specified permission.
     *
     * @param permission the permission to check (e.g., "order:create")
     * @return true if the current user has the permission
     */
    public static boolean hasPermission(String permission) {
        return getCurrentPermissions().contains(permission);
    }
}
