package net.chrisrichardson.ftgo.security.util;

/**
 * Constants used across the FTGO security configuration.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // utility class
    }

    /** Authorization header name. */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer token prefix. */
    public static final String BEARER_PREFIX = "Bearer ";

    /** Role prefix used by Spring Security. */
    public static final String ROLE_PREFIX = "ROLE_";

    /** Default public actuator paths (health + info). */
    public static final String[] PUBLIC_ACTUATOR_PATHS = {
            "/actuator/health",
            "/actuator/info"
    };

    /** Common public API paths that do not require authentication. */
    public static final String[] DEFAULT_PUBLIC_PATHS = {
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/webjars/**"
    };
}
