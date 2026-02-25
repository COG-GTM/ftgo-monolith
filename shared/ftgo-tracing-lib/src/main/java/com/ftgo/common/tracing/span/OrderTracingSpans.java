package com.ftgo.common.tracing.span;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Custom spans for the order creation flow.
 *
 * <p>Provides pre-defined span builders for tracing the order lifecycle
 * across service boundaries:</p>
 * <ol>
 *   <li>Consumer validation (Consumer Service)</li>
 *   <li>Restaurant availability check (Restaurant Service)</li>
 *   <li>Order creation (Order Service)</li>
 *   <li>Order acceptance/rejection</li>
 * </ol>
 *
 * <h3>Usage</h3>
 * <pre>
 * {@literal @}Autowired
 * private OrderTracingSpans orderSpans;
 *
 * public void createOrder(CreateOrderRequest request) {
 *     Span span = orderSpans.startOrderCreation(request.getOrderId());
 *     try (Tracer.SpanInScope ws = orderSpans.getTracer().withSpan(span)) {
 *         // ... order creation logic
 *     } finally {
 *         span.end();
 *     }
 * }
 * </pre>
 */
@Component
@ConditionalOnBean(Tracer.class)
public class OrderTracingSpans {

    private static final String ORDER_PREFIX = "ftgo.order";

    private final Tracer tracer;

    public OrderTracingSpans(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Returns the underlying Tracer for span-in-scope management.
     */
    public Tracer getTracer() {
        return tracer;
    }

    /**
     * Starts a span for the order creation flow.
     *
     * @param orderId the order identifier (nullable for new orders)
     * @return a new span representing the order creation operation
     */
    public Span startOrderCreation(String orderId) {
        Span span = tracer.nextSpan()
                .name(ORDER_PREFIX + ".create")
                .tag("ftgo.operation", "order.create");
        if (orderId != null) {
            span.tag("ftgo.order.id", orderId);
        }
        return span.start();
    }

    /**
     * Starts a span for consumer validation during order creation.
     *
     * @param consumerId the consumer identifier
     * @return a new span representing the consumer validation
     */
    public Span startConsumerValidation(String consumerId) {
        Span span = tracer.nextSpan()
                .name(ORDER_PREFIX + ".validate-consumer")
                .tag("ftgo.operation", "order.validate-consumer");
        if (consumerId != null) {
            span.tag("ftgo.consumer.id", consumerId);
        }
        return span.start();
    }

    /**
     * Starts a span for restaurant availability check during order creation.
     *
     * @param restaurantId the restaurant identifier
     * @return a new span representing the restaurant check
     */
    public Span startRestaurantCheck(String restaurantId) {
        Span span = tracer.nextSpan()
                .name(ORDER_PREFIX + ".check-restaurant")
                .tag("ftgo.operation", "order.check-restaurant");
        if (restaurantId != null) {
            span.tag("ftgo.restaurant.id", restaurantId);
        }
        return span.start();
    }

    /**
     * Starts a span for order acceptance processing.
     *
     * @param orderId the order identifier
     * @return a new span representing order acceptance
     */
    public Span startOrderAcceptance(String orderId) {
        Span span = tracer.nextSpan()
                .name(ORDER_PREFIX + ".accept")
                .tag("ftgo.operation", "order.accept");
        if (orderId != null) {
            span.tag("ftgo.order.id", orderId);
        }
        return span.start();
    }

    /**
     * Starts a span for order rejection processing.
     *
     * @param orderId the order identifier
     * @param reason  the rejection reason
     * @return a new span representing order rejection
     */
    public Span startOrderRejection(String orderId, String reason) {
        Span span = tracer.nextSpan()
                .name(ORDER_PREFIX + ".reject")
                .tag("ftgo.operation", "order.reject");
        if (orderId != null) {
            span.tag("ftgo.order.id", orderId);
        }
        if (reason != null) {
            span.tag("ftgo.order.rejection.reason", reason);
        }
        return span.start();
    }
}
