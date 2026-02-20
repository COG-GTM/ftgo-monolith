package com.ftgo.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ftgo.gateway")
public class GatewayProperties {

    private List<ServiceRoute> routes = new ArrayList<>();
    private RateLimit defaultRateLimit = new RateLimit();
    private Cors cors = new Cors();

    public List<ServiceRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(List<ServiceRoute> routes) {
        this.routes = routes;
    }

    public RateLimit getDefaultRateLimit() {
        return defaultRateLimit;
    }

    public void setDefaultRateLimit(RateLimit defaultRateLimit) {
        this.defaultRateLimit = defaultRateLimit;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public static class ServiceRoute {

        private String id;
        private String path;
        private String uri;
        private int stripPrefix = 1;
        private boolean authRequired = true;
        private List<String> requiredRoles = new ArrayList<>();
        private int rateLimit = 100;
        private int burstCapacity = 150;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public int getStripPrefix() {
            return stripPrefix;
        }

        public void setStripPrefix(int stripPrefix) {
            this.stripPrefix = stripPrefix;
        }

        public boolean isAuthRequired() {
            return authRequired;
        }

        public void setAuthRequired(boolean authRequired) {
            this.authRequired = authRequired;
        }

        public List<String> getRequiredRoles() {
            return requiredRoles;
        }

        public void setRequiredRoles(List<String> requiredRoles) {
            this.requiredRoles = requiredRoles;
        }

        public int getRateLimit() {
            return rateLimit;
        }

        public void setRateLimit(int rateLimit) {
            this.rateLimit = rateLimit;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }

    public static class RateLimit {

        private int requestsPerSecond = 100;
        private int burstCapacity = 150;

        public int getRequestsPerSecond() {
            return requestsPerSecond;
        }

        public void setRequestsPerSecond(int requestsPerSecond) {
            this.requestsPerSecond = requestsPerSecond;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }

    public static class Cors {

        private List<String> allowedOrigins = List.of("http://localhost:3000");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = List.of("X-Correlation-Id");
        private boolean allowCredentials = true;
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
