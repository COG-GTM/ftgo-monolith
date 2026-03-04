package net.chrisrichardson.ftgo.resilience.retry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Configures Resilience4j Retry for FTGO services.
 *
 * <p>Default behavior:
 * <ul>
 *   <li>3 retry attempts</li>
 *   <li>Exponential backoff: 1s, 2s, 4s</li>
 *   <li>Retries on {@link IOException} and {@link TimeoutException}</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(FtgoResilienceProperties.class)
public class FtgoRetryConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoRetryConfiguration.class);

    /**
     * Creates the default retry configuration from properties.
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryConfig ftgoRetryConfig(FtgoResilienceProperties properties) {
        FtgoResilienceProperties.RetryProperties retryProps = properties.getRetry();

        RetryConfig.Builder<?> builder = RetryConfig.custom()
                .maxAttempts(retryProps.getMaxAttempts())
                .retryExceptions(IOException.class, TimeoutException.class);

        if (retryProps.isExponentialBackoff()) {
            builder.intervalFunction(io.github.resilience4j.core.IntervalFunction
                    .ofExponentialBackoff(
                            retryProps.getWaitDuration().toMillis(),
                            retryProps.getMultiplier()));
        } else {
            builder.waitDuration(retryProps.getWaitDuration());
        }

        RetryConfig config = builder.build();

        log.info("FTGO Retry config: maxAttempts={}, waitDuration={}ms, exponentialBackoff={}, multiplier={}",
                retryProps.getMaxAttempts(),
                retryProps.getWaitDuration().toMillis(),
                retryProps.isExponentialBackoff(),
                retryProps.getMultiplier());

        return config;
    }

    /**
     * Creates the retry registry with the default configuration
     * and pre-registers retries for known FTGO services.
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry ftgoRetryRegistry(RetryConfig config) {
        RetryRegistry registry = RetryRegistry.of(config);

        String[] serviceNames = {
                "order-service", "consumer-service",
                "restaurant-service", "courier-service"
        };

        for (String serviceName : serviceNames) {
            Retry retry = registry.retry(serviceName);
            retry.getEventPublisher()
                    .onRetry(event ->
                            log.warn("Retry '{}' attempt #{} due to: {}",
                                    event.getName(),
                                    event.getNumberOfRetryAttempts(),
                                    event.getLastThrowable().getMessage()))
                    .onError(event ->
                            log.error("Retry '{}' exhausted after {} attempts: {}",
                                    event.getName(),
                                    event.getNumberOfRetryAttempts(),
                                    event.getLastThrowable().getMessage()));
        }

        log.info("FTGO Retry registry initialized with {} pre-registered retries",
                serviceNames.length);
        return registry;
    }
}
