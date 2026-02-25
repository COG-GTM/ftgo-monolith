package com.ftgo.common.resilience.bulkhead;

import com.ftgo.common.resilience.config.ResilienceProperties;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Resilience4j bulkhead pattern.
 *
 * <p>Provides thread pool isolation for inter-service calls, limiting
 * the number of concurrent calls to each downstream service to prevent
 * resource exhaustion.</p>
 *
 * <p>Usage with annotations:</p>
 * <pre>
 * &#64;Bulkhead(name = "consumer-service", fallbackMethod = "fallback")
 * public Consumer getConsumer(long consumerId) { ... }
 * </pre>
 */
@Configuration
@ConditionalOnClass(Bulkhead.class)
public class BulkheadConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BulkheadConfiguration.class);

    /**
     * Creates a default {@link BulkheadConfig} with FTGO-standard settings.
     *
     * <ul>
     *   <li>Max concurrent calls: 25</li>
     *   <li>Max wait duration: 500ms</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean(BulkheadConfig.class)
    public BulkheadConfig defaultBulkheadConfig(ResilienceProperties properties) {
        ResilienceProperties.BulkheadProperties bulkheadProps = properties.getBulkhead();

        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(bulkheadProps.getMaxConcurrentCalls())
                .maxWaitDuration(Duration.ofMillis(bulkheadProps.getMaxWaitDurationMillis()))
                .build();

        log.info("FTGO Bulkhead configured: maxConcurrentCalls={}, maxWaitDuration={}ms",
                bulkheadProps.getMaxConcurrentCalls(),
                bulkheadProps.getMaxWaitDurationMillis());

        return config;
    }

    /**
     * Creates a {@link BulkheadRegistry} with the default configuration.
     */
    @Bean
    @ConditionalOnMissingBean(BulkheadRegistry.class)
    public BulkheadRegistry bulkheadRegistry(BulkheadConfig defaultBulkheadConfig) {
        return BulkheadRegistry.of(defaultBulkheadConfig);
    }
}
