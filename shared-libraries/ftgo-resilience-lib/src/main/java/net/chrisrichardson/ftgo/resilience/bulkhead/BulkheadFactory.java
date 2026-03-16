package net.chrisrichardson.ftgo.resilience.bulkhead;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Factory for creating and managing Resilience4j bulkhead instances
 * for thread pool isolation between downstream service calls.
 */
public class BulkheadFactory {

    private static final Logger logger = LoggerFactory.getLogger(BulkheadFactory.class);

    private final BulkheadRegistry registry;

    public BulkheadFactory(BulkheadRegistry registry) {
        this.registry = registry;
    }

    /**
     * Returns a bulkhead for the given service name, creating one if it does not exist.
     *
     * @param serviceName logical name of the downstream service
     * @return the bulkhead instance
     */
    public Bulkhead getBulkhead(String serviceName) {
        Bulkhead bulkhead = registry.bulkhead(serviceName);
        logger.debug("Retrieved bulkhead for service: {}", serviceName);
        return bulkhead;
    }

    /**
     * Creates a bulkhead with a custom configuration override.
     *
     * @param serviceName logical name of the downstream service
     * @param properties  custom configuration properties
     * @return the bulkhead instance
     */
    public Bulkhead getBulkhead(String serviceName, BulkheadConfigurationProperties properties) {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(properties.getMaxConcurrentCalls())
                .maxWaitTime(properties.getMaxWaitTimeMillis())
                .build();

        Bulkhead bulkhead = registry.bulkhead(serviceName, config);
        logger.info("Created bulkhead for service: {} (maxConcurrent={}, maxWait={}ms)",
                serviceName,
                properties.getMaxConcurrentCalls(),
                properties.getMaxWaitTimeMillis());
        return bulkhead;
    }

    /**
     * Returns the underlying bulkhead registry.
     */
    public BulkheadRegistry getRegistry() {
        return registry;
    }
}
