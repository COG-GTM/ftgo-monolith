package com.ftgo.courier.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom business metrics for the FTGO Courier Service.
 *
 * <p>Registers and exposes the following metrics:
 * <ul>
 *   <li>{@code ftgo.couriers.available} - Gauge of currently available couriers</li>
 *   <li>{@code ftgo.couriers.unavailable} - Gauge of currently unavailable couriers</li>
 *   <li>{@code ftgo.deliveries.assigned} - Counter of deliveries assigned to couriers</li>
 *   <li>{@code ftgo.deliveries.completed} - Counter of deliveries completed</li>
 *   <li>{@code ftgo.deliveries.failed} - Counter of failed deliveries</li>
 *   <li>{@code ftgo.delivery.time} - Timer for delivery duration</li>
 * </ul>
 */
@Component
public class CourierMetrics {

    private final AtomicInteger couriersAvailable;
    private final AtomicInteger couriersUnavailable;
    private final Counter deliveriesAssigned;
    private final Counter deliveriesCompleted;
    private final Counter deliveriesFailed;
    private final Timer deliveryTime;

    public CourierMetrics(MeterRegistry registry) {
        this.couriersAvailable = new AtomicInteger(0);
        Gauge.builder("ftgo.couriers.available", couriersAvailable, AtomicInteger::get)
                .description("Number of currently available couriers")
                .tag("service", "courier-service")
                .register(registry);

        this.couriersUnavailable = new AtomicInteger(0);
        Gauge.builder("ftgo.couriers.unavailable", couriersUnavailable, AtomicInteger::get)
                .description("Number of currently unavailable couriers")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveriesAssigned = Counter.builder("ftgo.deliveries.assigned")
                .description("Total number of deliveries assigned to couriers")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveriesCompleted = Counter.builder("ftgo.deliveries.completed")
                .description("Total number of deliveries completed")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveriesFailed = Counter.builder("ftgo.deliveries.failed")
                .description("Total number of failed deliveries")
                .tag("service", "courier-service")
                .register(registry);

        this.deliveryTime = Timer.builder("ftgo.delivery.time")
                .description("Time taken from delivery assignment to completion")
                .tag("service", "courier-service")
                .register(registry);
    }

    public void setCouriersAvailable(int count) {
        couriersAvailable.set(count);
    }

    public void incrementCouriersAvailable() {
        couriersAvailable.incrementAndGet();
    }

    public void decrementCouriersAvailable() {
        couriersAvailable.decrementAndGet();
    }

    public void setCouriersUnavailable(int count) {
        couriersUnavailable.set(count);
    }

    public void incrementCouriersUnavailable() {
        couriersUnavailable.incrementAndGet();
    }

    public void decrementCouriersUnavailable() {
        couriersUnavailable.decrementAndGet();
    }

    public void incrementDeliveriesAssigned() {
        deliveriesAssigned.increment();
    }

    public void incrementDeliveriesCompleted() {
        deliveriesCompleted.increment();
    }

    public void incrementDeliveriesFailed() {
        deliveriesFailed.increment();
    }

    public Timer.Sample startDeliveryTimer() {
        return Timer.start();
    }

    public void stopDeliveryTimer(Timer.Sample sample) {
        sample.stop(deliveryTime);
    }
}
