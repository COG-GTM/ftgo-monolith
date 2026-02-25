package com.ftgo.gateway.ratelimit;

import com.ftgo.gateway.config.GatewayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate limiting configuration for the API Gateway.
 * <p>
 * Provides two rate limiter implementations:
 * <ul>
 *   <li><strong>In-memory</strong> (default): Token bucket per key,
 *       suitable for single-instance and dev environments.</li>
 *   <li><strong>Redis-backed</strong>: Distributed rate limiting via
 *       Spring Cloud Gateway's built-in Redis rate limiter, activated
 *       with {@code ftgo.gateway.rate-limit.redis-enabled=true}.</li>
 * </ul>
 * </p>
 * <p>
 * Key resolution strategy: Requests are keyed by the client IP address.
 * If the {@code X-Forwarded-For} header is present, the first entry is
 * used (important when behind a load balancer).
 * </p>
 */
@Configuration
public class RateLimitConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RateLimitConfiguration.class);

    /**
     * Key resolver that uses the client's IP address.
     * Falls back to "anonymous" if the address cannot be determined.
     */
    @Bean
    public KeyResolver clientIpKeyResolver() {
        return exchange -> {
            // Prefer X-Forwarded-For header (set by load balancers)
            String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                // Take the first IP (original client)
                String clientIp = forwardedFor.split(",")[0].trim();
                return Mono.just(clientIp);
            }

            // Fall back to remote address
            if (exchange.getRequest().getRemoteAddress() != null) {
                return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
            }

            return Mono.just("anonymous");
        };
    }

    /**
     * In-memory rate limiter bean (used when Redis is disabled).
     */
    @Bean
    @Primary
    @ConditionalOnProperty(
            name = "ftgo.gateway.rate-limit.redis-enabled",
            havingValue = "false",
            matchIfMissing = true)
    public RateLimiter<?> inMemoryRateLimiter(GatewayProperties gatewayProperties) {
        log.info("Using in-memory rate limiter (Redis disabled)");
        return new InMemoryRateLimiter(gatewayProperties);
    }
}
