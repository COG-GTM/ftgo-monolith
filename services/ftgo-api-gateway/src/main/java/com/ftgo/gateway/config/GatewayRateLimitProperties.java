package com.ftgo.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Rate limiting configuration properties for the API Gateway.
 *
 * <p>Properties are prefixed with {@code ftgo.gateway.rate-limit}.
 *
 * <p>Example configuration:
 * <pre>
 * ftgo.gateway.rate-limit.default-replenish-rate=10
 * ftgo.gateway.rate-limit.default-burst-capacity=20
 * ftgo.gateway.rate-limit.per-route.orders.replenish-rate=50
 * ftgo.gateway.rate-limit.per-route.orders.burst-capacity=100
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "ftgo.gateway.rate-limit")
public class GatewayRateLimitProperties {

    /**
     * Default tokens replenished per second. Default: 10.
     */
    private int defaultReplenishRate = 10;

    /**
     * Default maximum burst capacity. Default: 20.
     */
    private int defaultBurstCapacity = 20;

    /**
     * Default requested tokens per request. Default: 1.
     */
    private int defaultRequestedTokens = 1;

    /**
     * Per-route rate limit overrides keyed by route identifier.
     */
    private Map<String, RouteRateLimit> perRoute = new HashMap<>();

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

    public Map<String, RouteRateLimit> getPerRoute() {
        return perRoute;
    }

    public void setPerRoute(Map<String, RouteRateLimit> perRoute) {
        this.perRoute = perRoute;
    }

    /**
     * Per-route rate limit configuration.
     */
    public static class RouteRateLimit {
        private int replenishRate;
        private int burstCapacity;
        private int requestedTokens = 1;

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

        public int getRequestedTokens() {
            return requestedTokens;
        }

        public void setRequestedTokens(int requestedTokens) {
            this.requestedTokens = requestedTokens;
        }
    }
}
