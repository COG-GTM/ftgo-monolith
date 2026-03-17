package net.chrisrichardson.ftgo.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for FTGO security settings.
 *
 * <p>Properties are bound under the {@code ftgo.security} prefix.
 */
@ConfigurationProperties(prefix = "ftgo.security")
public class FtgoSecurityProperties {

    private final Cors cors = new Cors();
    private List<String> publicPaths = new ArrayList<>(List.of(
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    ));

    public Cors getCors() {
        return cors;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    /**
     * CORS configuration properties.
     */
    public static class Cors {

        private List<String> allowedOrigins = new ArrayList<>(List.of("*"));
        private List<String> allowedMethods = new ArrayList<>(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
        private List<String> exposedHeaders = new ArrayList<>();
        private boolean allowCredentials = false;
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
    }
}
