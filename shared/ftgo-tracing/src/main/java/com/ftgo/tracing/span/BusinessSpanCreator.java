package com.ftgo.tracing.span;

import com.ftgo.tracing.TracingConstants;
import com.ftgo.tracing.config.FtgoTracingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates custom spans for key business operations in FTGO microservices.
 *
 * <p>This class provides a centralized way to define and manage custom spans
 * for important business operations. When Micrometer Tracing is active,
 * services should use the {@code @Observed} annotation or the
 * {@code ObservationRegistry} for creating spans. This class provides
 * standard span naming conventions for the FTGO domain.</p>
 *
 * <h3>Span Naming Conventions:</h3>
 * <ul>
 *   <li>{@code ftgo.order.create} - Order creation workflow</li>
 *   <li>{@code ftgo.order.accept} - Restaurant accepts an order</li>
 *   <li>{@code ftgo.order.deliver} - Order delivery process</li>
 *   <li>{@code ftgo.consumer.validate} - Consumer validation</li>
 *   <li>{@code ftgo.restaurant.findAvailable} - Find available restaurants</li>
 *   <li>{@code ftgo.courier.assign} - Courier assignment</li>
 *   <li>{@code ftgo.gateway.route} - API Gateway routing</li>
 * </ul>
 *
 * <h3>Usage with Micrometer Observation API:</h3>
 * <pre>
 * // Using @Observed annotation (recommended)
 * &#64;Observed(name = "ftgo.order.create",
 *           contextualName = "create-order",
 *           lowCardinalityKeyValues = {"order.type", "delivery"})
 * public Order createOrder(CreateOrderRequest request) { ... }
 *
 * // Using ObservationRegistry programmatically
 * Observation.createNotStarted(businessSpanCreator.orderSpanName("create"), registry)
 *     .lowCardinalityKeyValue("order.type", "delivery")
 *     .observe(() -&gt; orderService.createOrder(request));
 * </pre>
 */
public class BusinessSpanCreator {

    private static final Logger log = LoggerFactory.getLogger(BusinessSpanCreator.class);

    private final FtgoTracingProperties properties;

    public BusinessSpanCreator(FtgoTracingProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates a span name for order service operations.
     *
     * @param operation the operation name (e.g., "create", "accept", "cancel")
     * @return the fully qualified span name (e.g., "ftgo.order.create")
     */
    public String orderSpanName(String operation) {
        return TracingConstants.SPAN_ORDER_PREFIX + "." + operation;
    }

    /**
     * Creates a span name for consumer service operations.
     *
     * @param operation the operation name (e.g., "validate", "register")
     * @return the fully qualified span name (e.g., "ftgo.consumer.validate")
     */
    public String consumerSpanName(String operation) {
        return TracingConstants.SPAN_CONSUMER_PREFIX + "." + operation;
    }

    /**
     * Creates a span name for restaurant service operations.
     *
     * @param operation the operation name (e.g., "findAvailable", "acceptOrder")
     * @return the fully qualified span name (e.g., "ftgo.restaurant.findAvailable")
     */
    public String restaurantSpanName(String operation) {
        return TracingConstants.SPAN_RESTAURANT_PREFIX + "." + operation;
    }

    /**
     * Creates a span name for courier service operations.
     *
     * @param operation the operation name (e.g., "assign", "updateLocation")
     * @return the fully qualified span name (e.g., "ftgo.courier.assign")
     */
    public String courierSpanName(String operation) {
        return TracingConstants.SPAN_COURIER_PREFIX + "." + operation;
    }

    /**
     * Creates a span name for API Gateway operations.
     *
     * @param operation the operation name (e.g., "route", "rateLimit")
     * @return the fully qualified span name (e.g., "ftgo.gateway.route")
     */
    public String gatewaySpanName(String operation) {
        return TracingConstants.SPAN_GATEWAY_PREFIX + "." + operation;
    }

    /**
     * Returns the configured service name for span tagging.
     *
     * @return the service name
     */
    public String getServiceName() {
        return properties.getServiceName();
    }
}
