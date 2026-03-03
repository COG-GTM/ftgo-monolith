package net.chrisrichardson.ftgo.security.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class providing common security operations for FTGO microservices.
 *
 * <p>Provides convenient static methods for accessing the current security
 * context, checking authentication status, and retrieving user information.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * // Get the current authenticated username
 * Optional&lt;String&gt; username = SecurityUtils.getCurrentUsername();
 *
 * // Check if the current user is authenticated
 * boolean authenticated = SecurityUtils.isAuthenticated();
 *
 * // Check if the current user has a specific role
 * boolean isAdmin = SecurityUtils.hasRole("ADMIN");
 * </pre>
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns the current {@link Authentication} object from the security context.
     *
     * @return an Optional containing the Authentication, or empty if not available
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    /**
     * Returns the username of the currently authenticated user.
     *
     * @return an Optional containing the username, or empty if not authenticated
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentAuthentication()
                .filter(SecurityUtils::isNonAnonymous)
                .map(Authentication::getName);
    }

    /**
     * Checks if the current user is authenticated (not anonymous).
     *
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication()
                .filter(SecurityUtils::isNonAnonymous)
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    /**
     * Checks if the current user has the specified role.
     * The role name is automatically prefixed with "ROLE_" if not already.
     *
     * @param role the role to check (e.g., "ADMIN" or "ROLE_ADMIN")
     * @return true if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(normalizedRole::equals);
    }

    /**
     * Checks if the current user has the specified authority.
     *
     * @param authority the authority to check
     * @return true if the user has the authority, false otherwise
     */
    public static boolean hasAuthority(String authority) {
        return getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    /**
     * Returns the authorities of the currently authenticated user.
     *
     * @return the collection of granted authorities, or an empty collection
     */
    public static Collection<? extends GrantedAuthority> getAuthorities() {
        return getCurrentAuthentication()
                .filter(SecurityUtils::isNonAnonymous)
                .map(Authentication::getAuthorities)
                .orElse(Collections.emptyList());
    }

    /**
     * Returns the role names of the currently authenticated user
     * (without the "ROLE_" prefix).
     *
     * @return the collection of role names
     */
    public static Collection<String> getRoles() {
        return getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .collect(Collectors.toList());
    }

    private static boolean isNonAnonymous(Authentication authentication) {
        return !(authentication instanceof AnonymousAuthenticationToken);
    }
}
