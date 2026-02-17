package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class ConsumerMetrics {

    private final Counter consumersRegistered;
    private final Counter consumerValidations;
    private final Counter consumerValidationFailures;
    private final Timer consumerValidationTime;
    private final Counter consumerUpdates;

    public ConsumerMetrics(MeterRegistry registry) {
        this.consumersRegistered = Counter.builder(FtgoMetricsConstants.PREFIX_CONSUMER + ".registered.total")
                .description("Total number of consumers registered")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "consumer-service")
                .register(registry);

        this.consumerValidations = Counter.builder(FtgoMetricsConstants.PREFIX_CONSUMER + ".validations.total")
                .description("Total number of consumer validations performed")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "consumer-service")
                .register(registry);

        this.consumerValidationFailures = Counter.builder(FtgoMetricsConstants.PREFIX_CONSUMER + ".validations.failed.total")
                .description("Total number of consumer validation failures")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "consumer-service")
                .register(registry);

        this.consumerValidationTime = Timer.builder(FtgoMetricsConstants.PREFIX_CONSUMER + ".validation.duration")
                .description("Time taken to validate a consumer")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "consumer-service")
                .register(registry);

        this.consumerUpdates = Counter.builder(FtgoMetricsConstants.PREFIX_CONSUMER + ".updates.total")
                .description("Total number of consumer profile updates")
                .tag(FtgoMetricsConstants.TAG_SERVICE, "consumer-service")
                .register(registry);
    }

    public Counter getConsumersRegistered() {
        return consumersRegistered;
    }

    public Counter getConsumerValidations() {
        return consumerValidations;
    }

    public Counter getConsumerValidationFailures() {
        return consumerValidationFailures;
    }

    public Timer getConsumerValidationTime() {
        return consumerValidationTime;
    }

    public Counter getConsumerUpdates() {
        return consumerUpdates;
    }
}
