package net.chrisrichardson.ftgo.resilience.retry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.IntervalFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Factory for creating and managing Resilience4j retry instances
 * with exponential backoff for inter-service communication.
 */
public class RetryFactory {

    private static final Logger logger = LoggerFactory.getLogger(RetryFactory.class);

    private final RetryRegistry registry;

    public RetryFactory(RetryRegistry registry) {
        this.registry = registry;
    }

    /**
     * Returns a retry instance for the given service name, creating one if it does not exist.
     *
     * @param serviceName logical name of the downstream service
     * @return the retry instance
     */
    public Retry getRetry(String serviceName) {
        Retry retry = registry.retry(serviceName);
        logger.debug("Retrieved retry for service: {}", serviceName);
        return retry;
    }

    /**
     * Creates a retry instance with a custom configuration using exponential backoff.
     *
     * @param serviceName logical name of the downstream service
     * @param properties  custom configuration properties
     * @return the retry instance
     */
    public Retry getRetry(String serviceName, RetryConfigurationProperties properties) {
        IntervalFunction intervalFunction = IntervalFunction.ofExponentialBackoff(
                properties.getInitialIntervalMillis(),
                properties.getMultiplier());

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(properties.getMaxAttempts())
                .intervalFunction(intervalFunction)
                .build();

        Retry retry = registry.retry(serviceName, config);
        logger.info("Created retry for service: {} (maxAttempts={}, initialInterval={}ms, multiplier={})",
                serviceName,
                properties.getMaxAttempts(),
                properties.getInitialIntervalMillis(),
                properties.getMultiplier());
        return retry;
    }

    /**
     * Returns the underlying retry registry.
     */
    public RetryRegistry getRegistry() {
        return registry;
    }
}
