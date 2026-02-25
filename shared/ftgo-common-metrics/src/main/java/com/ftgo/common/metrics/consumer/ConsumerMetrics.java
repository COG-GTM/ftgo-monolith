package com.ftgo.common.metrics.consumer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Custom business metrics for the Consumer Service.
 *
 * <p>Metrics provided:</p>
 * <ul>
 *   <li>{@code consumers.registered} - Counter of consumers registered</li>
 *   <li>{@code consumers.validated} - Counter of consumer validations performed</li>
 * </ul>
 *
 * <p>Activate by setting {@code ftgo.metrics.consumer.enabled=true} in application properties.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.metrics.consumer.enabled", havingValue = "true", matchIfMissing = false)
public class ConsumerMetrics {

    private final MeterRegistry meterRegistry;

    private Counter consumersRegistered;
    private Counter consumersValidated;

    public ConsumerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        consumersRegistered = Counter.builder("consumers.registered")
                .description("Total number of consumers registered")
                .tag("service", "consumer-service")
                .register(meterRegistry);

        consumersValidated = Counter.builder("consumers.validated")
                .description("Total number of consumer validations performed")
                .tag("service", "consumer-service")
                .register(meterRegistry);
    }

    /**
     * Records a new consumer registration.
     */
    public void recordConsumerRegistered() {
        consumersRegistered.increment();
    }

    /**
     * Records a consumer validation event.
     */
    public void recordConsumerValidated() {
        consumersValidated.increment();
    }
}
