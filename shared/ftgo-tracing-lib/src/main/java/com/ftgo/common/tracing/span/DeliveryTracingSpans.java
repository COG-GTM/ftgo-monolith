package com.ftgo.common.tracing.span;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Custom spans for the delivery assignment flow.
 *
 * <p>Provides pre-defined span builders for tracing the delivery lifecycle:</p>
 * <ol>
 *   <li>Courier availability search</li>
 *   <li>Delivery assignment</li>
 *   <li>Delivery pickup</li>
 *   <li>Delivery completion</li>
 * </ol>
 *
 * <h3>Usage</h3>
 * <pre>
 * {@literal @}Autowired
 * private DeliveryTracingSpans deliverySpans;
 *
 * public void assignDelivery(String orderId) {
 *     Span span = deliverySpans.startDeliveryAssignment(orderId);
 *     try (Tracer.SpanInScope ws = deliverySpans.getTracer().withSpan(span)) {
 *         // ... delivery assignment logic
 *     } finally {
 *         span.end();
 *     }
 * }
 * </pre>
 */
@Component
@ConditionalOnBean(Tracer.class)
public class DeliveryTracingSpans {

    private static final String DELIVERY_PREFIX = "ftgo.delivery";

    private final Tracer tracer;

    public DeliveryTracingSpans(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Returns the underlying Tracer for span-in-scope management.
     */
    public Tracer getTracer() {
        return tracer;
    }

    /**
     * Starts a span for searching available couriers.
     *
     * @param orderId the order requiring delivery
     * @return a new span representing the courier search
     */
    public Span startCourierSearch(String orderId) {
        Span span = tracer.nextSpan()
                .name(DELIVERY_PREFIX + ".search-courier")
                .tag("ftgo.operation", "delivery.search-courier");
        if (orderId != null) {
            span.tag("ftgo.order.id", orderId);
        }
        return span.start();
    }

    /**
     * Starts a span for assigning a delivery to a courier.
     *
     * @param orderId the order identifier
     * @return a new span representing the delivery assignment
     */
    public Span startDeliveryAssignment(String orderId) {
        Span span = tracer.nextSpan()
                .name(DELIVERY_PREFIX + ".assign")
                .tag("ftgo.operation", "delivery.assign");
        if (orderId != null) {
            span.tag("ftgo.order.id", orderId);
        }
        return span.start();
    }

    /**
     * Starts a span for delivery pickup by courier.
     *
     * @param orderId   the order identifier
     * @param courierId the courier identifier
     * @return a new span representing the pickup event
     */
    public Span startDeliveryPickup(String orderId, String courierId) {
        Span span = tracer.nextSpan()
                .name(DELIVERY_PREFIX + ".pickup")
                .tag("ftgo.operation", "delivery.pickup");
        if (orderId != null) {
            span.tag("ftgo.order.id", orderId);
        }
        if (courierId != null) {
            span.tag("ftgo.courier.id", courierId);
        }
        return span.start();
    }

    /**
     * Starts a span for delivery completion.
     *
     * @param orderId   the order identifier
     * @param courierId the courier identifier
     * @return a new span representing the delivery completion
     */
    public Span startDeliveryCompletion(String orderId, String courierId) {
        Span span = tracer.nextSpan()
                .name(DELIVERY_PREFIX + ".complete")
                .tag("ftgo.operation", "delivery.complete");
        if (orderId != null) {
            span.tag("ftgo.order.id", orderId);
        }
        if (courierId != null) {
            span.tag("ftgo.courier.id", courierId);
        }
        return span.start();
    }
}
