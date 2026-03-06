package com.ftgo.gateway.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitGatewayFilterFactory
        extends AbstractGatewayFilterFactory<RateLimitGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(RateLimitGatewayFilterFactory.class);
    private static final long BUCKET_TTL_NANOS = TimeUnit.MINUTES.toNanos(10);

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitGatewayFilterFactory() {
        super(Config.class);
        ScheduledExecutorService evictionExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-eviction");
            t.setDaemon(true);
            return t;
        });
        evictionExecutor.scheduleAtFixedRate(this::evictExpiredBuckets, 5, 5, TimeUnit.MINUTES);
    }

    RateLimitGatewayFilterFactory(boolean skipEviction) {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String key = resolveKey(exchange);
            TokenBucket bucket = buckets.computeIfAbsent(key,
                    k -> new TokenBucket(config.getRequestsPerSecond(), config.getBurstCapacity()));

            if (!bucket.tryConsume()) {
                log.warn("Rate limit exceeded for key: {}", key);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After", "1");
                exchange.getResponse().getHeaders().add("X-Gateway-Error", "Rate limit exceeded");
                return exchange.getResponse().setComplete();
            }

            long remaining = bucket.getAvailableTokens();
            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));
            exchange.getResponse().getHeaders().add("X-RateLimit-Limit",
                    String.valueOf(config.getBurstCapacity()));

            return chain.filter(exchange);
        };
    }

    private void evictExpiredBuckets() {
        long now = System.nanoTime();
        Iterator<Map.Entry<String, TokenBucket>> it = buckets.entrySet().iterator();
        int evicted = 0;
        while (it.hasNext()) {
            Map.Entry<String, TokenBucket> entry = it.next();
            if (now - entry.getValue().getLastAccessTime() > BUCKET_TTL_NANOS) {
                it.remove();
                evicted++;
            }
        }
        if (evicted > 0) {
            log.debug("Evicted {} expired rate limit buckets, remaining: {}", evicted, buckets.size());
        }
    }

    private String resolveKey(ServerWebExchange exchange) {
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        String path = exchange.getRequest().getURI().getPath();

        String routeSegment = extractRouteSegment(path);
        return clientIp + ":" + routeSegment;
    }

    private String extractRouteSegment(String path) {
        if (path == null || path.length() <= 1) {
            return "default";
        }
        String[] segments = path.split("/");
        if (segments.length > 1) {
            return segments[1];
        }
        return "default";
    }

    static class TokenBucket {

        private final int requestsPerSecond;
        private final int burstCapacity;
        private final AtomicLong availableTokens;
        private volatile long lastRefillTimestamp;
        private volatile long lastAccessTime;

        TokenBucket(int requestsPerSecond, int burstCapacity) {
            this.requestsPerSecond = requestsPerSecond;
            this.burstCapacity = burstCapacity;
            this.availableTokens = new AtomicLong(burstCapacity);
            this.lastRefillTimestamp = System.nanoTime();
            this.lastAccessTime = System.nanoTime();
        }

        synchronized boolean tryConsume() {
            refill();
            lastAccessTime = System.nanoTime();
            long current = availableTokens.get();
            if (current > 0) {
                availableTokens.decrementAndGet();
                return true;
            }
            return false;
        }

        long getAvailableTokens() {
            return availableTokens.get();
        }

        long getLastAccessTime() {
            return lastAccessTime;
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillTimestamp;
            long tokensToAdd = (elapsed * requestsPerSecond) / 1_000_000_000L;

            if (tokensToAdd > 0) {
                long newTokens = Math.min(burstCapacity, availableTokens.get() + tokensToAdd);
                availableTokens.set(newTokens);
                lastRefillTimestamp = now;
            }
        }
    }

    public static class Config {

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
}
