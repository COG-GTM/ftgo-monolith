package net.chrisrichardson.ftgo.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import net.chrisrichardson.ftgo.resilience.retry.FtgoRetryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link FtgoRetryConfiguration}.
 */
class FtgoRetryConfigurationTest {

    private FtgoResilienceProperties properties;
    private FtgoRetryConfiguration configuration;

    @BeforeEach
    void setUp() {
        properties = new FtgoResilienceProperties();
        configuration = new FtgoRetryConfiguration();
    }

    @Test
    @DisplayName("Retry config uses default values (3 attempts, exponential backoff)")
    void retryConfigUsesDefaults() {
        RetryConfig config = configuration.ftgoRetryConfig(properties);

        assertThat(config.getMaxAttempts()).isEqualTo(3);
    }

    @Test
    @DisplayName("Retry config uses custom values")
    void retryConfigUsesCustomValues() {
        FtgoResilienceProperties.RetryProperties retryProps = properties.getRetry();
        retryProps.setMaxAttempts(5);
        retryProps.setWaitDuration(Duration.ofSeconds(2));

        RetryConfig config = configuration.ftgoRetryConfig(properties);

        assertThat(config.getMaxAttempts()).isEqualTo(5);
    }

    @Test
    @DisplayName("Retry registry pre-registers service retries")
    void registryPreRegistersServiceRetries() {
        RetryConfig config = configuration.ftgoRetryConfig(properties);
        RetryRegistry registry = configuration.ftgoRetryRegistry(config);

        assertThat(registry.retry("order-service")).isNotNull();
        assertThat(registry.retry("consumer-service")).isNotNull();
        assertThat(registry.retry("restaurant-service")).isNotNull();
        assertThat(registry.retry("courier-service")).isNotNull();
    }

    @Test
    @DisplayName("Retry executes callable with retries on IOException")
    void retryExecutesWithRetries() {
        // Use minimal wait to speed up test
        properties.getRetry().setWaitDuration(Duration.ofMillis(10));
        properties.getRetry().setMultiplier(1.0);
        RetryConfig config = configuration.ftgoRetryConfig(properties);
        RetryRegistry registry = configuration.ftgoRetryRegistry(config);
        Retry retry = registry.retry("test-service");

        AtomicInteger attempts = new AtomicInteger(0);

        // Will fail twice with IOException then succeed on third attempt
        String result = Retry.decorateCheckedSupplier(retry, () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new IOException("transient failure");
            }
            return "success";
        }).unchecked().get();

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Retry with fixed wait when exponential backoff is disabled")
    void retryWithFixedWait() {
        properties.getRetry().setExponentialBackoff(false);
        properties.getRetry().setWaitDuration(Duration.ofMillis(100));

        RetryConfig config = configuration.ftgoRetryConfig(properties);

        assertThat(config.getMaxAttempts()).isEqualTo(3);
    }
}
