package com.ftgo.tracing;

/**
 * Constants used throughout the FTGO distributed tracing framework.
 * Defines standard configuration property prefixes, MDC keys, and
 * default values for consistent tracing across all microservices.
 */
public final class TracingConstants {

    private TracingConstants() {
        // Utility class - prevent instantiation
    }

    // -------------------------------------------------------------------------
    // Configuration Property Prefixes
    // -------------------------------------------------------------------------

    /** Root configuration prefix for FTGO tracing properties. */
    public static final String CONFIG_PREFIX = "ftgo.tracing";

    // -------------------------------------------------------------------------
    // MDC Keys - Used to populate structured log fields via SLF4J MDC
    // -------------------------------------------------------------------------

    /** MDC key for the distributed trace ID (from Brave/Zipkin). */
    public static final String MDC_TRACE_ID = "traceId";

    /** MDC key for the distributed span ID within a trace. */
    public static final String MDC_SPAN_ID = "spanId";

    /** MDC key for the parent span ID. */
    public static final String MDC_PARENT_SPAN_ID = "parentSpanId";

    /** MDC key indicating whether this span was sampled. */
    public static final String MDC_SAMPLED = "sampled";

    // -------------------------------------------------------------------------
    // Propagation Headers - B3 Propagation (Brave/Zipkin standard)
    // -------------------------------------------------------------------------

    /** B3 single-header propagation format. */
    public static final String HEADER_B3 = "b3";

    /** B3 multi-header: trace ID. */
    public static final String HEADER_X_B3_TRACE_ID = "X-B3-TraceId";

    /** B3 multi-header: span ID. */
    public static final String HEADER_X_B3_SPAN_ID = "X-B3-SpanId";

    /** B3 multi-header: parent span ID. */
    public static final String HEADER_X_B3_PARENT_SPAN_ID = "X-B3-ParentSpanId";

    /** B3 multi-header: sampled flag. */
    public static final String HEADER_X_B3_SAMPLED = "X-B3-Sampled";

    // -------------------------------------------------------------------------
    // W3C Trace Context Headers
    // -------------------------------------------------------------------------

    /** W3C Trace Context: traceparent header. */
    public static final String HEADER_TRACEPARENT = "traceparent";

    /** W3C Trace Context: tracestate header. */
    public static final String HEADER_TRACESTATE = "tracestate";

    // -------------------------------------------------------------------------
    // Default Values
    // -------------------------------------------------------------------------

    /** Default Zipkin endpoint URL for local development. */
    public static final String DEFAULT_ZIPKIN_ENDPOINT = "http://localhost:9411/api/v2/spans";

    /** Default sampling probability for development (100%). */
    public static final float DEFAULT_SAMPLING_PROBABILITY_DEV = 1.0f;

    /** Default sampling probability for production (10%). */
    public static final float DEFAULT_SAMPLING_PROBABILITY_PROD = 0.1f;

    // -------------------------------------------------------------------------
    // Span Names - Standard span names for business operations
    // -------------------------------------------------------------------------

    /** Span name prefix for order operations. */
    public static final String SPAN_ORDER_PREFIX = "ftgo.order";

    /** Span name prefix for consumer operations. */
    public static final String SPAN_CONSUMER_PREFIX = "ftgo.consumer";

    /** Span name prefix for restaurant operations. */
    public static final String SPAN_RESTAURANT_PREFIX = "ftgo.restaurant";

    /** Span name prefix for courier operations. */
    public static final String SPAN_COURIER_PREFIX = "ftgo.courier";

    /** Span name prefix for API Gateway operations. */
    public static final String SPAN_GATEWAY_PREFIX = "ftgo.gateway";
}
