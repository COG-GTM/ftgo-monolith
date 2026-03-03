package net.chrisrichardson.ftgo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Redis-backed rate limiting configuration.
 *
 * <p>Uses a token bucket algorithm backed by Redis. Rate limiting is applied
 * per client, identified by:
 * <ol>
 *   <li>X-API-Key header (if present)</li>
 *   <li>JWT subject claim (if authenticated)</li>
 *   <li>Client IP address (fallback)</li>
 * </ol>
 */
@Configuration
public class RateLimiterConfig {

    @Value("${ftgo.gateway.rate-limit.default-replenish-rate:10}")
    private int defaultReplenishRate;

    @Value("${ftgo.gateway.rate-limit.default-burst-capacity:20}")
    private int defaultBurstCapacity;

    @Value("${ftgo.gateway.rate-limit.default-requested-tokens:1}")
    private int defaultRequestedTokens;

    /**
     * Default Redis rate limiter using token bucket algorithm.
     * <ul>
     *   <li>replenishRate: tokens added per second</li>
     *   <li>burstCapacity: maximum tokens in the bucket</li>
     *   <li>requestedTokens: tokens consumed per request</li>
     * </ul>
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(defaultReplenishRate, defaultBurstCapacity, defaultRequestedTokens);
    }

    /**
     * Resolves the rate limiting key from the request.
     * Priority: X-API-Key header > authenticated principal > client IP.
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            // 1. Check for API key header
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isBlank()) {
                return Mono.just("apikey:" + apiKey);
            }

            // 2. Check for authenticated principal (set by JWT filter)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }

            // 3. Fallback to client IP
            String clientIp = "unknown";
            if (exchange.getRequest().getRemoteAddress() != null) {
                clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }
            return Mono.just("ip:" + clientIp);
        };
    }
}
