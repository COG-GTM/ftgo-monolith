package net.chrisrichardson.ftgo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configurable security properties for FTGO microservices.
 *
 * <p>Properties are bound from {@code ftgo.security.*} in application configuration.
 */
@ConfigurationProperties(prefix = "ftgo.security")
public class FtgoSecurityProperties {

    /**
     * Whether security is enabled. Defaults to true.
     */
    private boolean enabled = true;

    /**
     * CORS configuration.
     */
    private Cors cors = new Cors();

    /**
     * Paths that should be publicly accessible without authentication.
     */
    private List<String> publicPaths = List.of();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public static class Cors {

        /**
         * Allowed origins for CORS requests.
         */
        private List<String> allowedOrigins = List.of();

        /**
         * Allowed HTTP methods for CORS requests.
         */
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

        /**
         * Allowed headers for CORS requests.
         */
        private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "X-Requested-With");

        /**
         * Whether credentials (cookies, authorization headers) are allowed in CORS requests.
         */
        private boolean allowCredentials = true;

        /**
         * Max age (in seconds) for the CORS preflight cache.
         */
        private long maxAge = 3600;

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
    }
}
