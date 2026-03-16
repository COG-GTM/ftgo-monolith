package net.chrisrichardson.ftgo.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Utility class for accessing the current Spring Security context.
 *
 * <p>Provides convenient static methods for common security checks
 * in service and controller code.
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
        // utility class
    }

    /**
     * Returns the current {@link Authentication} if present.
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Returns the principal name of the currently authenticated user, or {@code "anonymous"}.
     */
    public static String getCurrentUsername() {
        return getCurrentAuthentication()
                .map(Authentication::getName)
                .orElse("anonymous");
    }

    /**
     * Returns {@code true} if there is an authenticated (non-anonymous) user in the security context.
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication()
                .map(auth -> auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                .orElse(false);
    }

    /**
     * Returns the authorities granted to the current user, or an empty collection.
     */
    public static Collection<? extends GrantedAuthority> getCurrentAuthorities() {
        return getCurrentAuthentication()
                .map(Authentication::getAuthorities)
                .orElse(Collections.emptyList());
    }

    /**
     * Returns {@code true} if the current user has the specified authority.
     *
     * @param authority the authority string to check (e.g., {@code "ROLE_ADMIN"})
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
