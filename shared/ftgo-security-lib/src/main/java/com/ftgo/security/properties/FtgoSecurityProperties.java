package com.ftgo.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for FTGO security settings.
 * <p>
 * Bind via {@code ftgo.security.*} in application.properties or application.yml.
 * </p>
 *
 * <pre>
 * ftgo.security.cors.allowed-origins=https://api-gateway.ftgo.com
 * ftgo.security.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
 * ftgo.security.public-paths=/actuator/health,/actuator/info
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.security")
public class FtgoSecurityProperties {

    private final Cors cors = new Cors();
    private List<String> publicPaths = new ArrayList<>(List.of(
            "/actuator/health",
            "/actuator/info"
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
     * CORS configuration properties under {@code ftgo.security.cors.*}.
     */
    public static class Cors {

        private List<String> allowedOrigins = new ArrayList<>(List.of("*"));
        private List<String> allowedMethods = new ArrayList<>(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        private List<String> allowedHeaders = new ArrayList<>(List.of(
                "Authorization", "Content-Type", "X-Requested-With",
                "Accept", "Origin", "X-Correlation-Id"
        ));
        private List<String> exposedHeaders = new ArrayList<>(List.of(
                "X-Correlation-Id"
        ));
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
