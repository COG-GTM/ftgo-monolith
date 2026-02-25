package com.ftgo.gateway.ratelimit;

import com.ftgo.gateway.config.GatewayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory token bucket rate limiter for environments without Redis.
 * <p>
 * This implementation provides a simple per-key token bucket algorithm.
 * Tokens are replenished at a fixed rate and consumed on each request.
 * When the bucket is empty, requests are rejected with HTTP 429.
 * </p>
 * <p>
 * This is suitable for single-instance deployments and development.
 * For production multi-instance deployments, use Redis-backed rate limiting
 * by enabling {@code ftgo.gateway.rate-limit.redis-enabled=true}.
 * </p>
 */
public class InMemoryRateLimiter extends AbstractRateLimiter<InMemoryRateLimiter.Config> {

    private static final Logger log = LoggerFactory.getLogger(InMemoryRateLimiter.class);
    private static final String CONFIGURATION_PROPERTY_NAME = "in-memory-rate-limiter";

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final GatewayProperties gatewayProperties;

    public InMemoryRateLimiter(GatewayProperties gatewayProperties) {
        super(Config.class, CONFIGURATION_PROPERTY_NAME, null);
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        GatewayProperties.RateLimit rateLimitProps = gatewayProperties.getRateLimit();
        if (!rateLimitProps.isEnabled()) {
            return Mono.just(new Response(true, Collections.emptyMap()));
        }

        // Check for endpoint-specific configuration
        GatewayProperties.EndpointRateLimit endpointConfig = rateLimitProps.getEndpoints().get(routeId);
        final int replenishRate = (endpointConfig != null)
                ? endpointConfig.getReplenishRate()
                : rateLimitProps.getDefaultReplenishRate();
        final int burstCapacity = (endpointConfig != null)
                ? endpointConfig.getBurstCapacity()
                : rateLimitProps.getDefaultBurstCapacity();

        String key = routeId + ":" + id;
        TokenBucket bucket = buckets.computeIfAbsent(key,
                k -> new TokenBucket(burstCapacity, replenishRate));

        boolean allowed = bucket.tryConsume();

        Map<String, String> headers = Map.of(
                "X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()),
                "X-RateLimit-Burst-Capacity", String.valueOf(burstCapacity),
                "X-RateLimit-Replenish-Rate", String.valueOf(replenishRate)
        );

        if (!allowed) {
            log.warn("Rate limit exceeded for key '{}' on route '{}'", id, routeId);
        }

        return Mono.just(new Response(allowed, headers));
    }

    /**
     * Simple token bucket implementation.
     */
    static class TokenBucket {
        private final int capacity;
        private final int refillRate;
        private final AtomicLong tokens;
        private volatile long lastRefillTime;

        TokenBucket(int capacity, int refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            long current = tokens.get();
            if (current > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        long getAvailableTokens() {
            return tokens.get();
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            long tokensToAdd = (elapsed / 1000) * refillRate;
            if (tokensToAdd > 0) {
                long newTokens = Math.min(capacity, tokens.get() + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime = now;
            }
        }
    }

    /**
     * Configuration class for the in-memory rate limiter.
     */
    public static class Config {
        // Can be extended with per-route config
    }
}
