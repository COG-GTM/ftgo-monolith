package com.ftgo.common.resilience.ratelimiter;

import com.ftgo.common.resilience.config.ResilienceProperties;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Resilience4j rate limiting.
 *
 * <p>Protects against cascading failures by limiting the rate of calls
 * to downstream services.</p>
 *
 * <p>Usage with annotations:</p>
 * <pre>
 * &#64;RateLimiter(name = "consumer-service", fallbackMethod = "fallback")
 * public Consumer getConsumer(long consumerId) { ... }
 * </pre>
 */
@Configuration
@ConditionalOnClass(RateLimiter.class)
public class RateLimiterConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterConfiguration.class);

    /**
     * Creates a default {@link RateLimiterConfig} with FTGO-standard settings.
     *
     * <ul>
     *   <li>Limit for period: 50 calls per second</li>
     *   <li>Refresh period: 1 second</li>
     *   <li>Timeout duration: 500ms</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean(RateLimiterConfig.class)
    public RateLimiterConfig defaultRateLimiterConfig(ResilienceProperties properties) {
        ResilienceProperties.RateLimiterProperties rlProps = properties.getRateLimiter();

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(rlProps.getLimitForPeriod())
                .limitRefreshPeriod(Duration.ofMillis(rlProps.getLimitRefreshPeriodMillis()))
                .timeoutDuration(Duration.ofMillis(rlProps.getTimeoutDurationMillis()))
                .build();

        log.info("FTGO Rate Limiter configured: limitForPeriod={}, refreshPeriod={}ms, timeout={}ms",
                rlProps.getLimitForPeriod(),
                rlProps.getLimitRefreshPeriodMillis(),
                rlProps.getTimeoutDurationMillis());

        return config;
    }

    /**
     * Creates a {@link RateLimiterRegistry} with the default configuration.
     */
    @Bean
    @ConditionalOnMissingBean(RateLimiterRegistry.class)
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig defaultRateLimiterConfig) {
        return RateLimiterRegistry.of(defaultRateLimiterConfig);
    }
}
