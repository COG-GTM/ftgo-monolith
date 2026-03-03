package com.ftgo.consumer.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for the FTGO Consumer Service.
 *
 * <p>Registers and exposes the following metrics:
 * <ul>
 *   <li>{@code ftgo.consumers.registered} - Counter of consumers registered</li>
 *   <li>{@code ftgo.consumers.validated} - Counter of successful consumer validations</li>
 *   <li>{@code ftgo.consumers.validation.failed} - Counter of failed consumer validations</li>
 *   <li>{@code ftgo.consumers.updated} - Counter of consumer profile updates</li>
 *   <li>{@code ftgo.consumer.validation.time} - Timer for consumer validation duration</li>
 * </ul>
 */
@Component
public class ConsumerMetrics {

    private final Counter consumersRegistered;
    private final Counter consumersValidated;
    private final Counter consumersValidationFailed;
    private final Counter consumersUpdated;
    private final Timer consumerValidationTime;

    public ConsumerMetrics(MeterRegistry registry) {
        this.consumersRegistered = Counter.builder("ftgo.consumers.registered")
                .description("Total number of consumers registered")
                .tag("service", "consumer-service")
                .register(registry);

        this.consumersValidated = Counter.builder("ftgo.consumers.validated")
                .description("Total number of successful consumer validations")
                .tag("service", "consumer-service")
                .register(registry);

        this.consumersValidationFailed = Counter.builder("ftgo.consumers.validation.failed")
                .description("Total number of failed consumer validations")
                .tag("service", "consumer-service")
                .register(registry);

        this.consumersUpdated = Counter.builder("ftgo.consumers.updated")
                .description("Total number of consumer profile updates")
                .tag("service", "consumer-service")
                .register(registry);

        this.consumerValidationTime = Timer.builder("ftgo.consumer.validation.time")
                .description("Time taken to validate a consumer for an order")
                .tag("service", "consumer-service")
                .register(registry);
    }

    public void incrementConsumersRegistered() {
        consumersRegistered.increment();
    }

    public void incrementConsumersValidated() {
        consumersValidated.increment();
    }

    public void incrementConsumersValidationFailed() {
        consumersValidationFailed.increment();
    }

    public void incrementConsumersUpdated() {
        consumersUpdated.increment();
    }

    public Timer.Sample startValidationTimer() {
        return Timer.start();
    }

    public void stopValidationTimer(Timer.Sample sample) {
        sample.stop(consumerValidationTime);
    }
}
