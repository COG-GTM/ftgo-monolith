package com.ftgo.gateway.filter;

import com.ftgo.gateway.config.GatewayProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiting filter backed by Caffeine cache.
 *
 * <p>Tracks request counts per client IP address using a sliding time window.
 * When a client exceeds the configured maximum number of requests within the
 * window, subsequent requests receive a {@code 429 Too Many Requests} response.
 *
 * <p>The remaining request count is returned in the {@code X-RateLimit-Remaining}
 * response header to help clients self-throttle.
 */
@Component
public class RateLimitingFilter implements GatewayFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final GatewayProperties gatewayProperties;
    private final Cache<String, AtomicInteger> requestCounts;

    public RateLimitingFilter(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
        this.requestCounts = Caffeine.newBuilder()
                .expireAfterWrite(gatewayProperties.getRateLimit().getWindowSeconds(), TimeUnit.SECONDS)
                .maximumSize(100_000)
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayProperties.getRateLimit().isEnabled()) {
            return chain.filter(exchange);
        }

        String clientId = resolveClientId(exchange);
        int maxRequests = gatewayProperties.getRateLimit().getMaxRequests();

        AtomicInteger counter = requestCounts.get(clientId, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        int remaining = Math.max(0, maxRequests - currentCount);
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(maxRequests));

        if (currentCount > maxRequests) {
            log.warn("Rate limit exceeded for client: {} (count: {}, limit: {})",
                    clientId, currentCount, maxRequests);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private String resolveClientId(ServerWebExchange exchange) {
        // Use X-Forwarded-For if behind a load balancer
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 3;
    }
}
