package net.chrisrichardson.ftgo.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Utility class for common security operations.
 *
 * <p>Provides convenience methods for accessing the current security context,
 * extracting authentication details, and checking authorization.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Prevent instantiation
    }

    /**
     * Returns the current {@link Authentication} from the security context, if present.
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Returns the username of the currently authenticated user, or empty if not authenticated.
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }

    /**
     * Returns {@code true} if there is an authenticated user in the current security context.
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication()
                .map(auth -> auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                .orElse(false);
    }

    /**
     * Returns the authorities granted to the current user, or an empty collection if not authenticated.
     */
    public static Collection<? extends GrantedAuthority> getCurrentAuthorities() {
        return getCurrentAuthentication()
                .map(Authentication::getAuthorities)
                .orElse(Collections.emptyList());
    }

    /**
     * Returns {@code true} if the current user has the specified authority/role.
     *
     * @param authority the authority to check (e.g., "ROLE_ADMIN")
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
