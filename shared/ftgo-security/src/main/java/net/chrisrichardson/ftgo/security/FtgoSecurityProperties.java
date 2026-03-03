package net.chrisrichardson.ftgo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for FTGO security settings.
 *
 * <p>These properties can be configured in application.yml or application.properties
 * using the prefix {@code ftgo.security}.</p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * ftgo:
 *   security:
 *     cors:
 *       allowed-origins:
 *         - http://localhost:3000
 *         - https://api-gateway.ftgo.com
 *       allowed-methods:
 *         - GET
 *         - POST
 *         - PUT
 *         - DELETE
 *     public-paths:
 *       - /actuator/health
 *       - /actuator/info
 *       - /api/public/**
 *     actuator:
 *       public-endpoints:
 *         - health
 *         - info
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.security")
public class FtgoSecurityProperties {

    /**
     * CORS configuration properties.
     */
    private final Cors cors = new Cors();

    /**
     * List of URL patterns that should be publicly accessible without authentication.
     * Defaults to actuator health and info endpoints.
     */
    private List<String> publicPaths = new ArrayList<>(Arrays.asList(
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info"
    ));

    /**
     * Actuator security configuration.
     */
    private final Actuator actuator = new Actuator();

    public Cors getCors() {
        return cors;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public Actuator getActuator() {
        return actuator;
    }

    /**
     * Returns the public paths as a String array for use with antMatchers.
     *
     * @return array of public path patterns
     */
    public String[] getPublicPathsArray() {
        return publicPaths.toArray(new String[0]);
    }

    /**
     * CORS configuration properties.
     */
    public static class Cors {

        /**
         * Whether CORS is enabled. Defaults to true.
         */
        private boolean enabled = true;

        /**
         * List of allowed origins for CORS requests.
         * Defaults to localhost origins for development.
         */
        private List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080"
        ));

        /**
         * List of allowed HTTP methods for CORS requests.
         */
        private List<String> allowedMethods = new ArrayList<>(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        /**
         * List of allowed headers for CORS requests.
         */
        private List<String> allowedHeaders = new ArrayList<>(Arrays.asList(
                "Authorization", "Content-Type", "Accept", "Origin",
                "X-Requested-With", "X-XSRF-TOKEN"
        ));

        /**
         * List of headers exposed to the client.
         */
        private List<String> exposedHeaders = new ArrayList<>(Arrays.asList(
                "Authorization", "X-Total-Count"
        ));

        /**
         * Whether credentials (cookies, authorization headers) are allowed.
         * Defaults to true.
         */
        private boolean allowCredentials = true;

        /**
         * Maximum age (in seconds) of the CORS preflight cache. Defaults to 3600 (1 hour).
         */
        private long maxAge = 3600L;

        /**
         * URL pattern to apply CORS to. Defaults to all API paths.
         */
        private String pattern = "/**";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }

    /**
     * Actuator security configuration properties.
     */
    public static class Actuator {

        /**
         * List of actuator endpoints that should be publicly accessible.
         * Defaults to health and info.
         */
        private List<String> publicEndpoints = new ArrayList<>(Arrays.asList(
                "health", "info"
        ));

        public List<String> getPublicEndpoints() {
            return publicEndpoints;
        }

        public void setPublicEndpoints(List<String> publicEndpoints) {
            this.publicEndpoints = publicEndpoints;
        }
    }
}
