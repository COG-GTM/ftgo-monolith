package net.chrisrichardson.ftgo.resilience.ratelimiter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Resilience4j Rate Limiter for FTGO services.
 *
 * <p>Limits the rate of calls to downstream services to prevent overload.
 *
 * <p>Default behavior:
 * <ul>
 *   <li>50 calls per second</li>
 *   <li>500ms timeout waiting for permission</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(FtgoResilienceProperties.class)
public class FtgoRateLimiterConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoRateLimiterConfiguration.class);

    /**
     * Creates the default rate limiter configuration from properties.
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiterConfig ftgoRateLimiterConfig(FtgoResilienceProperties properties) {
        FtgoResilienceProperties.RateLimiterProperties rlProps = properties.getRateLimiter();

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(rlProps.getLimitForPeriod())
                .limitRefreshPeriod(rlProps.getLimitRefreshPeriod())
                .timeoutDuration(rlProps.getTimeoutDuration())
                .build();

        log.info("FTGO Rate Limiter config: limitForPeriod={}, refreshPeriod={}ms, timeout={}ms",
                rlProps.getLimitForPeriod(),
                rlProps.getLimitRefreshPeriod().toMillis(),
                rlProps.getTimeoutDuration().toMillis());

        return config;
    }

    /**
     * Creates the rate limiter registry with the default configuration
     * and pre-registers rate limiters for known FTGO services.
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry ftgoRateLimiterRegistry(RateLimiterConfig config) {
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);

        String[] serviceNames = {
                "order-service", "consumer-service",
                "restaurant-service", "courier-service"
        };

        for (String serviceName : serviceNames) {
            RateLimiter rateLimiter = registry.rateLimiter(serviceName);
            rateLimiter.getEventPublisher()
                    .onFailure(event ->
                            log.warn("Rate limiter '{}' denied permission — rate limit exceeded",
                                    event.getRateLimiterName()));
        }

        log.info("FTGO Rate Limiter registry initialized with {} pre-registered limiters",
                serviceNames.length);
        return registry;
    }
}
