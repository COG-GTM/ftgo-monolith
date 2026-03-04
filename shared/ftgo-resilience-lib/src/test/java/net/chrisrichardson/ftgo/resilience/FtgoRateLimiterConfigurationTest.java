package net.chrisrichardson.ftgo.resilience;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import net.chrisrichardson.ftgo.resilience.ratelimiter.FtgoRateLimiterConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link FtgoRateLimiterConfiguration}.
 */
class FtgoRateLimiterConfigurationTest {

    private FtgoResilienceProperties properties;
    private FtgoRateLimiterConfiguration configuration;

    @BeforeEach
    void setUp() {
        properties = new FtgoResilienceProperties();
        configuration = new FtgoRateLimiterConfiguration();
    }

    @Test
    @DisplayName("Rate limiter config uses default values")
    void rateLimiterConfigUsesDefaults() {
        RateLimiterConfig config = configuration.ftgoRateLimiterConfig(properties);

        assertThat(config.getLimitForPeriod()).isEqualTo(50);
        assertThat(config.getLimitRefreshPeriod()).isEqualTo(Duration.ofSeconds(1));
        assertThat(config.getTimeoutDuration()).isEqualTo(Duration.ofMillis(500));
    }

    @Test
    @DisplayName("Rate limiter config uses custom values")
    void rateLimiterConfigUsesCustomValues() {
        FtgoResilienceProperties.RateLimiterProperties rlProps = properties.getRateLimiter();
        rlProps.setLimitForPeriod(100);
        rlProps.setLimitRefreshPeriod(Duration.ofSeconds(2));
        rlProps.setTimeoutDuration(Duration.ofMillis(1000));

        RateLimiterConfig config = configuration.ftgoRateLimiterConfig(properties);

        assertThat(config.getLimitForPeriod()).isEqualTo(100);
        assertThat(config.getLimitRefreshPeriod()).isEqualTo(Duration.ofSeconds(2));
        assertThat(config.getTimeoutDuration()).isEqualTo(Duration.ofMillis(1000));
    }

    @Test
    @DisplayName("Rate limiter registry pre-registers service limiters")
    void registryPreRegistersServiceLimiters() {
        RateLimiterConfig config = configuration.ftgoRateLimiterConfig(properties);
        RateLimiterRegistry registry = configuration.ftgoRateLimiterRegistry(config);

        assertThat(registry.rateLimiter("order-service")).isNotNull();
        assertThat(registry.rateLimiter("consumer-service")).isNotNull();
        assertThat(registry.rateLimiter("restaurant-service")).isNotNull();
        assertThat(registry.rateLimiter("courier-service")).isNotNull();
    }

    @Test
    @DisplayName("Rate limiter denies permission when limit exceeded")
    void rateLimiterDeniesWhenLimitExceeded() {
        properties.getRateLimiter().setLimitForPeriod(2);
        properties.getRateLimiter().setTimeoutDuration(Duration.ZERO);

        RateLimiterConfig config = configuration.ftgoRateLimiterConfig(properties);
        RateLimiterRegistry registry = configuration.ftgoRateLimiterRegistry(config);
        RateLimiter rateLimiter = registry.rateLimiter("test-service");

        // Consume all permissions using decorated runnable
        Runnable decorated = RateLimiter.decorateRunnable(rateLimiter, () -> {});
        decorated.run();
        decorated.run();

        // Next execution should fail with RequestNotPermitted
        assertThatThrownBy(decorated::run)
                .isInstanceOf(RequestNotPermitted.class);
    }
}
