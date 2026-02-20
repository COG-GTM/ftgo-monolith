package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

public class ConsumerMetrics {

    private final MeterRegistry registry;

    private Counter consumersRegisteredCounter;
    private Counter consumerVerificationsCounter;
    private Counter consumerVerificationFailuresCounter;
    private final AtomicLong activeConsumers = new AtomicLong(0);

    public ConsumerMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        consumersRegisteredCounter = Counter.builder("ftgo.consumers.registered")
                .description("Total number of consumers registered")
                .tag("domain", "consumer")
                .register(registry);

        consumerVerificationsCounter = Counter.builder("ftgo.consumers.verifications")
                .description("Total number of consumer verifications performed")
                .tag("domain", "consumer")
                .register(registry);

        consumerVerificationFailuresCounter = Counter.builder("ftgo.consumers.verification.failures")
                .description("Total number of consumer verification failures")
                .tag("domain", "consumer")
                .register(registry);

        Gauge.builder("ftgo.consumers.active", activeConsumers, AtomicLong::doubleValue)
                .description("Current number of active consumers")
                .tag("domain", "consumer")
                .register(registry);
    }

    public void incrementConsumersRegistered() {
        consumersRegisteredCounter.increment();
    }

    public void incrementConsumerVerifications() {
        consumerVerificationsCounter.increment();
    }

    public void incrementConsumerVerificationFailures() {
        consumerVerificationFailuresCounter.increment();
    }

    public void setActiveConsumers(long count) {
        activeConsumers.set(count);
    }
}
