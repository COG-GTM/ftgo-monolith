package net.chrisrichardson.ftgo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Utility class for common security operations.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns the current {@link Authentication} from the security context, if present.
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Returns the username of the currently authenticated principal, if present.
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentAuthentication()
                .map(Authentication::getName);
    }

    /**
     * Returns {@code true} if the current request is authenticated
     * (i.e., not anonymous).
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication()
                .filter(Authentication::isAuthenticated)
                .filter(auth -> !"anonymousUser".equals(auth.getPrincipal()))
                .isPresent();
    }
}
