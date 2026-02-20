package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

public class CourierMetrics {

    private final MeterRegistry registry;

    private Counter deliveriesCompletedCounter;
    private Counter deliveriesFailedCounter;
    private Counter couriersCreatedCounter;
    private Timer deliveryDurationTimer;
    private Timer deliveryPickupTimer;
    private DistributionSummary deliveryDistanceSummary;
    private final AtomicLong availableCouriers = new AtomicLong(0);

    public CourierMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        deliveriesCompletedCounter = Counter.builder("ftgo.deliveries.completed")
                .description("Total number of deliveries completed")
                .tag("domain", "courier")
                .register(registry);

        deliveriesFailedCounter = Counter.builder("ftgo.deliveries.failed")
                .description("Total number of deliveries that failed")
                .tag("domain", "courier")
                .register(registry);

        couriersCreatedCounter = Counter.builder("ftgo.couriers.created")
                .description("Total number of couriers created")
                .tag("domain", "courier")
                .register(registry);

        deliveryDurationTimer = Timer.builder("ftgo.deliveries.duration")
                .description("Time taken from pickup to delivery completion")
                .tag("domain", "courier")
                .register(registry);

        deliveryPickupTimer = Timer.builder("ftgo.deliveries.pickup.duration")
                .description("Time taken from assignment to pickup")
                .tag("domain", "courier")
                .register(registry);

        deliveryDistanceSummary = DistributionSummary.builder("ftgo.deliveries.distance")
                .description("Distribution of delivery distances in kilometers")
                .tag("domain", "courier")
                .baseUnit("km")
                .register(registry);

        Gauge.builder("ftgo.couriers.available", availableCouriers, AtomicLong::doubleValue)
                .description("Current number of available couriers")
                .tag("domain", "courier")
                .register(registry);
    }

    public void incrementDeliveriesCompleted() {
        deliveriesCompletedCounter.increment();
    }

    public void incrementDeliveriesFailed() {
        deliveriesFailedCounter.increment();
    }

    public void incrementCouriersCreated() {
        couriersCreatedCounter.increment();
    }

    public Timer getDeliveryDurationTimer() {
        return deliveryDurationTimer;
    }

    public Timer getDeliveryPickupTimer() {
        return deliveryPickupTimer;
    }

    public void recordDeliveryDistance(double distanceKm) {
        deliveryDistanceSummary.record(distanceKm);
    }

    public void setAvailableCouriers(long count) {
        availableCouriers.set(count);
    }
}
