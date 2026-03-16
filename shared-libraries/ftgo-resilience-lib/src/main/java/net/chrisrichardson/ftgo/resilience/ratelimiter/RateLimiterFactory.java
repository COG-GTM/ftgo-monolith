package net.chrisrichardson.ftgo.resilience.ratelimiter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Factory for creating and managing Resilience4j rate limiter instances
 * for controlling the rate of inter-service calls.
 */
public class RateLimiterFactory {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterFactory.class);

    private final RateLimiterRegistry registry;

    public RateLimiterFactory(RateLimiterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Returns a rate limiter for the given service name, creating one if it does not exist.
     *
     * @param serviceName logical name of the downstream service
     * @return the rate limiter instance
     */
    public RateLimiter getRateLimiter(String serviceName) {
        RateLimiter rateLimiter = registry.rateLimiter(serviceName);
        logger.debug("Retrieved rate limiter for service: {}", serviceName);
        return rateLimiter;
    }

    /**
     * Creates a rate limiter with a custom configuration override.
     *
     * @param serviceName logical name of the downstream service
     * @param properties  custom configuration properties
     * @return the rate limiter instance
     */
    public RateLimiter getRateLimiter(String serviceName,
                                      RateLimiterConfigurationProperties properties) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(properties.getLimitForPeriod())
                .limitRefreshPeriod(Duration.ofMillis(properties.getLimitRefreshPeriodMillis()))
                .timeoutDuration(Duration.ofMillis(properties.getTimeoutDurationMillis()))
                .build();

        RateLimiter rateLimiter = registry.rateLimiter(serviceName, config);
        logger.info("Created rate limiter for service: {} (limit={}/{}ms, timeout={}ms)",
                serviceName,
                properties.getLimitForPeriod(),
                properties.getLimitRefreshPeriodMillis(),
                properties.getTimeoutDurationMillis());
        return rateLimiter;
    }

    /**
     * Returns the underlying rate limiter registry.
     */
    public RateLimiterRegistry getRegistry() {
        return registry;
    }
}
