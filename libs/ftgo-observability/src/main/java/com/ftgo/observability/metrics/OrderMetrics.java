package com.ftgo.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;

public class OrderMetrics {

    private final MeterRegistry registry;

    private Counter ordersCreatedCounter;
    private Counter ordersApprovedCounter;
    private Counter ordersRejectedCounter;
    private Counter ordersCancelledCounter;
    private Counter ordersRevisedCounter;
    private Timer orderFulfillmentTimer;
    private Timer orderApprovalTimer;

    public OrderMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        ordersCreatedCounter = Counter.builder("ftgo.orders.created")
                .description("Total number of orders created")
                .tag("domain", "order")
                .register(registry);

        ordersApprovedCounter = Counter.builder("ftgo.orders.approved")
                .description("Total number of orders approved")
                .tag("domain", "order")
                .register(registry);

        ordersRejectedCounter = Counter.builder("ftgo.orders.rejected")
                .description("Total number of orders rejected")
                .tag("domain", "order")
                .register(registry);

        ordersCancelledCounter = Counter.builder("ftgo.orders.cancelled")
                .description("Total number of orders cancelled")
                .tag("domain", "order")
                .register(registry);

        ordersRevisedCounter = Counter.builder("ftgo.orders.revised")
                .description("Total number of orders revised")
                .tag("domain", "order")
                .register(registry);

        orderFulfillmentTimer = Timer.builder("ftgo.orders.fulfillment.duration")
                .description("Time taken to fulfill an order from creation to delivery")
                .tag("domain", "order")
                .register(registry);

        orderApprovalTimer = Timer.builder("ftgo.orders.approval.duration")
                .description("Time taken from order creation to approval")
                .tag("domain", "order")
                .register(registry);
    }

    public void incrementOrdersCreated() {
        ordersCreatedCounter.increment();
    }

    public void incrementOrdersApproved() {
        ordersApprovedCounter.increment();
    }

    public void incrementOrdersRejected() {
        ordersRejectedCounter.increment();
    }

    public void incrementOrdersCancelled() {
        ordersCancelledCounter.increment();
    }

    public void incrementOrdersRevised() {
        ordersRevisedCounter.increment();
    }

    public Timer getOrderFulfillmentTimer() {
        return orderFulfillmentTimer;
    }

    public Timer getOrderApprovalTimer() {
        return orderApprovalTimer;
    }
}
