package net.chrisrichardson.ftgo.resilience.bulkhead;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import net.chrisrichardson.ftgo.resilience.config.FtgoResilienceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Resilience4j Bulkhead for FTGO services.
 *
 * <p>Limits concurrent calls to each downstream service to prevent
 * thread pool exhaustion and cascade failures.
 *
 * <p>Default behavior:
 * <ul>
 *   <li>Maximum 25 concurrent calls per downstream service</li>
 *   <li>No wait duration (fail immediately if limit reached)</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(FtgoResilienceProperties.class)
public class FtgoBulkheadConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoBulkheadConfiguration.class);

    /**
     * Creates the default bulkhead configuration from properties.
     */
    @Bean
    @ConditionalOnMissingBean
    public BulkheadConfig ftgoBulkheadConfig(FtgoResilienceProperties properties) {
        FtgoResilienceProperties.BulkheadProperties bhProps = properties.getBulkhead();

        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(bhProps.getMaxConcurrentCalls())
                .maxWaitDuration(bhProps.getMaxWaitDuration())
                .build();

        log.info("FTGO Bulkhead config: maxConcurrentCalls={}, maxWaitDuration={}ms",
                bhProps.getMaxConcurrentCalls(),
                bhProps.getMaxWaitDuration().toMillis());

        return config;
    }

    /**
     * Creates the bulkhead registry with the default configuration
     * and pre-registers bulkheads for known FTGO services.
     */
    @Bean
    @ConditionalOnMissingBean
    public BulkheadRegistry ftgoBulkheadRegistry(BulkheadConfig config) {
        BulkheadRegistry registry = BulkheadRegistry.of(config);

        String[] serviceNames = {
                "order-service", "consumer-service",
                "restaurant-service", "courier-service"
        };

        for (String serviceName : serviceNames) {
            Bulkhead bulkhead = registry.bulkhead(serviceName);
            bulkhead.getEventPublisher()
                    .onCallRejected(event ->
                            log.warn("Bulkhead '{}' rejected call — max concurrent limit reached",
                                    event.getBulkheadName()))
                    .onCallFinished(event ->
                            log.trace("Bulkhead '{}' call finished",
                                    event.getBulkheadName()));
        }

        log.info("FTGO Bulkhead registry initialized with {} pre-registered bulkheads",
                serviceNames.length);
        return registry;
    }
}
