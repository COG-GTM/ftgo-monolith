package net.chrisrichardson.ftgo.jwt.util;

import net.chrisrichardson.ftgo.jwt.model.FtgoUserContext;

import java.util.Optional;

/**
 * Thread-local holder for the current authenticated user context.
 *
 * <p>This class provides convenient access to the {@link FtgoUserContext}
 * populated by the {@link net.chrisrichardson.ftgo.jwt.filter.JwtTokenAuthenticationFilter}.
 * It is similar to Spring's {@code SecurityContextHolder} but provides
 * direct access to FTGO-specific user claims without needing to cast or
 * unwrap the Spring Security authentication object.</p>
 *
 * <p>Example usage in service layer:</p>
 * <pre>
 * // Get the current user ID
 * String userId = JwtUserContextHolder.getCurrentUserId()
 *     .orElseThrow(() -&gt; new UnauthorizedException("Not authenticated"));
 *
 * // Check if the user has a specific role
 * boolean isAdmin = JwtUserContextHolder.getCurrentUser()
 *     .map(user -&gt; user.hasRole("ADMIN"))
 *     .orElse(false);
 * </pre>
 *
 * <p><strong>Thread Safety:</strong> The context is stored in a {@link ThreadLocal}
 * and is automatically cleared after each request by the authentication filter.
 * Do not store references to the user context beyond the request scope.</p>
 */
public final class JwtUserContextHolder {

    private static final ThreadLocal<FtgoUserContext> CONTEXT = new ThreadLocal<>();

    private JwtUserContextHolder() {
        // Utility class - prevent instantiation
    }

    /**
     * Sets the current user context for the current thread.
     *
     * @param userContext the user context to set
     */
    public static void setCurrentUser(FtgoUserContext userContext) {
        CONTEXT.set(userContext);
    }

    /**
     * Returns the current user context, if available.
     *
     * @return an Optional containing the user context, or empty if not authenticated
     */
    public static Optional<FtgoUserContext> getCurrentUser() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * Returns the current user context or throws if not available.
     *
     * @return the current user context
     * @throws IllegalStateException if no user context is set
     */
    public static FtgoUserContext requireCurrentUser() {
        FtgoUserContext context = CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException(
                    "No authenticated user context available. "
                            + "Ensure the request passed through JwtTokenAuthenticationFilter.");
        }
        return context;
    }

    /**
     * Returns the current user's ID, if available.
     *
     * @return an Optional containing the user ID, or empty if not authenticated
     */
    public static Optional<String> getCurrentUserId() {
        return getCurrentUser().map(FtgoUserContext::getUserId);
    }

    /**
     * Returns the current user's username, if available.
     *
     * @return an Optional containing the username, or empty if not authenticated
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(FtgoUserContext::getUsername);
    }

    /**
     * Checks if a user context is currently set (user is authenticated).
     *
     * @return true if a user context is present
     */
    public static boolean isAuthenticated() {
        return CONTEXT.get() != null;
    }

    /**
     * Clears the current user context from the thread-local.
     * This should be called at the end of each request.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
