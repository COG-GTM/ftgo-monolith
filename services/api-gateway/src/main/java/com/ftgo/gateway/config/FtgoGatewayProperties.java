package com.ftgo.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Type-safe configuration properties for the API Gateway.
 *
 * <p>Binds to the {@code gateway} prefix in application properties/YAML,
 * providing centralized access to JWT settings, rate limiting parameters,
 * and downstream service URLs.
 */
@Component
@ConfigurationProperties(prefix = "gateway")
public class FtgoGatewayProperties {

    private Jwt jwt = new Jwt();
    private RateLimit rateLimit = new RateLimit();
    private Services services = new Services();

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

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public static class Jwt {
        private String secret = "ftgo-secret-key-for-jwt-validation-minimum-256-bits-long";
        private boolean enabled = true;
        private String headerName = "Authorization";
        private String tokenPrefix = "Bearer ";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

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

        public String getTokenPrefix() {
            return tokenPrefix;
        }

        public void setTokenPrefix(String tokenPrefix) {
            this.tokenPrefix = tokenPrefix;
        }
    }

    public static class RateLimit {
        private int maxRequests = 100;
        private int windowSeconds = 60;
        private boolean enabled = true;

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public int getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(int windowSeconds) {
            this.windowSeconds = windowSeconds;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Services {
        private String orderServiceUrl = "http://order-service:8081";
        private String consumerServiceUrl = "http://consumer-service:8082";
        private String restaurantServiceUrl = "http://restaurant-service:8083";
        private String courierServiceUrl = "http://courier-service:8084";

        public String getOrderServiceUrl() {
            return orderServiceUrl;
        }

        public void setOrderServiceUrl(String orderServiceUrl) {
            this.orderServiceUrl = orderServiceUrl;
        }

        public String getConsumerServiceUrl() {
            return consumerServiceUrl;
        }

        public void setConsumerServiceUrl(String consumerServiceUrl) {
            this.consumerServiceUrl = consumerServiceUrl;
        }

        public String getRestaurantServiceUrl() {
            return restaurantServiceUrl;
        }

        public void setRestaurantServiceUrl(String restaurantServiceUrl) {
            this.restaurantServiceUrl = restaurantServiceUrl;
        }

        public String getCourierServiceUrl() {
            return courierServiceUrl;
        }

        public void setCourierServiceUrl(String courierServiceUrl) {
            this.courierServiceUrl = courierServiceUrl;
        }
    }
}
