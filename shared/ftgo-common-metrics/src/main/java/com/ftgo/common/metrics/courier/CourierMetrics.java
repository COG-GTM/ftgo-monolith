package com.ftgo.common.metrics.courier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * Custom business metrics for the Courier Service.
 *
 * <p>Metrics provided:</p>
 * <ul>
 *   <li>{@code deliveries.assigned} - Counter of deliveries assigned to couriers</li>
 *   <li>{@code deliveries.completed} - Counter of deliveries completed</li>
 *   <li>{@code deliveries.average_time} - Timer tracking delivery duration</li>
 * </ul>
 *
 * <p>Activate by setting {@code ftgo.metrics.courier.enabled=true} in application properties.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.metrics.courier.enabled", havingValue = "true", matchIfMissing = false)
public class CourierMetrics {

    private final MeterRegistry meterRegistry;

    private Counter deliveriesAssigned;
    private Counter deliveriesCompleted;
    private Timer deliveryTime;

    public CourierMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        deliveriesAssigned = Counter.builder("deliveries.assigned")
                .description("Total number of deliveries assigned to couriers")
                .tag("service", "courier-service")
                .register(meterRegistry);

        deliveriesCompleted = Counter.builder("deliveries.completed")
                .description("Total number of deliveries completed")
                .tag("service", "courier-service")
                .register(meterRegistry);

        deliveryTime = Timer.builder("deliveries.average_time")
                .description("Time taken to complete deliveries")
                .tag("service", "courier-service")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * Records a delivery assignment.
     */
    public void recordDeliveryAssigned() {
        deliveriesAssigned.increment();
    }

    /**
     * Records a delivery completion.
     */
    public void recordDeliveryCompleted() {
        deliveriesCompleted.increment();
    }

    /**
     * Records the duration of a completed delivery.
     *
     * @param duration the time taken to complete the delivery
     */
    public void recordDeliveryDuration(Duration duration) {
        deliveryTime.record(duration);
    }
}
