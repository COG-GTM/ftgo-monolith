package com.ftgo.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the FTGO API Gateway.
 * <p>
 * Bind via {@code ftgo.gateway.*} in application.yml.
 * </p>
 */
@ConfigurationProperties(prefix = "ftgo.gateway")
public class GatewayProperties {

    /** JWT configuration for gateway-level token validation. */
    private Jwt jwt = new Jwt();

    /** Rate limiting configuration. */
    private RateLimit rateLimit = new RateLimit();

    /** CORS configuration. */
    private Cors cors = new Cors();

    /** API versioning configuration. */
    private Versioning versioning = new Versioning();

    // --- Getters and Setters ---

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public Versioning getVersioning() {
        return versioning;
    }

    public void setVersioning(Versioning versioning) {
        this.versioning = versioning;
    }

    // --- Nested configuration classes ---

    public static class Jwt {
        private boolean enabled = true;
        private String secret;
        private String issuer = "ftgo-platform";
        private List<String> excludedPaths = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public List<String> getExcludedPaths() {
            return excludedPaths;
        }

        public void setExcludedPaths(List<String> excludedPaths) {
            this.excludedPaths = excludedPaths;
        }
    }

    public static class RateLimit {
        private boolean enabled = true;
        private int defaultReplenishRate = 10;
        private int defaultBurstCapacity = 20;
        private int defaultRequestedTokens = 1;
        private boolean redisEnabled = false;
        private Map<String, EndpointRateLimit> endpoints = new HashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getDefaultReplenishRate() {
            return defaultReplenishRate;
        }

        public void setDefaultReplenishRate(int defaultReplenishRate) {
            this.defaultReplenishRate = defaultReplenishRate;
        }

        public int getDefaultBurstCapacity() {
            return defaultBurstCapacity;
        }

        public void setDefaultBurstCapacity(int defaultBurstCapacity) {
            this.defaultBurstCapacity = defaultBurstCapacity;
        }

        public int getDefaultRequestedTokens() {
            return defaultRequestedTokens;
        }

        public void setDefaultRequestedTokens(int defaultRequestedTokens) {
            this.defaultRequestedTokens = defaultRequestedTokens;
        }

        public boolean isRedisEnabled() {
            return redisEnabled;
        }

        public void setRedisEnabled(boolean redisEnabled) {
            this.redisEnabled = redisEnabled;
        }

        public Map<String, EndpointRateLimit> getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(Map<String, EndpointRateLimit> endpoints) {
            this.endpoints = endpoints;
        }
    }

    public static class EndpointRateLimit {
        private int replenishRate;
        private int burstCapacity;

        public int getReplenishRate() {
            return replenishRate;
        }

        public void setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of("*");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private boolean allowCredentials = false;
        private Duration maxAge = Duration.ofHours(1);

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

        public Duration getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(Duration maxAge) {
            this.maxAge = maxAge;
        }
    }

    public static class Versioning {
        /** Whether to enable API versioning via header. */
        private boolean enabled = true;
        /** Header name for API version. */
        private String headerName = "X-API-Version";
        /** Default API version when header is not present. */
        private String defaultVersion = "v1";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getDefaultVersion() {
            return defaultVersion;
        }

        public void setDefaultVersion(String defaultVersion) {
            this.defaultVersion = defaultVersion;
        }
    }
}
