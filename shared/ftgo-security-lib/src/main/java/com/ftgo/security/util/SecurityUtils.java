package com.ftgo.security.util;

import com.ftgo.security.jwt.FtgoUserDetails;
import com.ftgo.security.jwt.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for common security operations.
 *
 * <p>Provides static helper methods for accessing the current security context,
 * extracting authentication details, and checking authorization. Includes
 * JWT-specific methods for accessing user details from JWT tokens.
 *
 * <p>JWT-specific methods:
 * <ul>
 *   <li>{@link #getCurrentUserDetails()} — full user details from JWT</li>
 *   <li>{@link #getCurrentUserId()} — user ID from JWT {@code sub} claim</li>
 *   <li>{@link #getCurrentRoles()} — roles from JWT {@code roles} claim</li>
 *   <li>{@link #getClaim(String)} — arbitrary claim from JWT</li>
 *   <li>{@link #getAllClaims()} — all JWT claims</li>
 * </ul>
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

    // =========================================================================
    // JWT-specific methods
    // =========================================================================

    /**
     * Returns the {@link FtgoUserDetails} from the current JWT authentication, if present.
     *
     * <p>This method only returns a value when the current authentication is a
     * {@link JwtAuthenticationToken} (i.e., the request was authenticated via JWT).
     *
     * @return an Optional containing the user details, or empty if not JWT-authenticated
     */
    public static Optional<FtgoUserDetails> getCurrentUserDetails() {
        return getCurrentAuthentication()
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .map(auth -> ((JwtAuthenticationToken) auth).getUserDetails());
    }

    /**
     * Returns the current user's ID from the JWT {@code sub} claim.
     *
     * @return an Optional containing the user ID, or empty if not JWT-authenticated
     */
    public static Optional<String> getCurrentUserId() {
        return getCurrentUserDetails()
                .map(FtgoUserDetails::getUserId);
    }

    /**
     * Returns the current user's roles from the JWT {@code roles} claim.
     *
     * @return a collection of role strings, or an empty collection if not JWT-authenticated
     */
    public static Collection<String> getCurrentRoles() {
        return getCurrentUserDetails()
                .map(FtgoUserDetails::getRoles)
                .orElse(Collections.emptyList());
    }

    /**
     * Returns a specific claim value from the current JWT token.
     *
     * @param claimName the claim name to retrieve
     * @return an Optional containing the claim value, or empty if not present or not JWT-authenticated
     */
    public static Optional<Object> getClaim(String claimName) {
        return getCurrentUserDetails()
                .map(details -> details.getClaim(claimName));
    }

    /**
     * Returns all claims from the current JWT token.
     *
     * @return an unmodifiable map of all JWT claims, or an empty map if not JWT-authenticated
     */
    public static Map<String, Object> getAllClaims() {
        return getCurrentUserDetails()
                .map(FtgoUserDetails::getClaims)
                .orElse(Collections.emptyMap());
    }
}
