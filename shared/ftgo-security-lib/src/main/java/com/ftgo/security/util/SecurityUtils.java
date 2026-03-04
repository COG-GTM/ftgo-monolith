package com.ftgo.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for common security operations.
 *
 * <p>Provides static helper methods for accessing the current security context,
 * extracting authentication details, and checking authorization.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class — prevent instantiation
    }

    /**
     * Returns the current {@link Authentication} from the security context, if present.
     *
     * @return an Optional containing the current Authentication, or empty if not authenticated
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    /**
     * Returns the username (principal name) of the currently authenticated user.
     *
     * @return an Optional containing the username, or empty if not authenticated
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentAuthentication()
                .map(Authentication::getName);
    }

    /**
     * Returns the authorities/roles of the currently authenticated user.
     *
     * @return a collection of authority strings, or an empty collection if not authenticated
     */
    public static Collection<String> getCurrentAuthorities() {
        return getCurrentAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toUnmodifiableList()))
                .orElse(Collections.emptyList());
    }

    /**
     * Checks whether the currently authenticated user has the specified authority/role.
     *
     * @param authority the authority to check (e.g., "ROLE_ADMIN")
     * @return true if the current user has the specified authority
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentAuthorities().contains(authority);
    }

    /**
     * Checks whether the current request is authenticated.
     *
     * @return true if there is an authenticated user in the security context
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication().isPresent();
    }
}
