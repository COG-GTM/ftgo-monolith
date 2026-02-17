package net.chrisrichardson.ftgo.observability.tracing;

import org.springframework.stereotype.Component;

/**
 * Propagates tracing context across service boundaries.
 * Ensures trace IDs and span IDs are passed via HTTP headers
 * (W3C Trace Context standard) for end-to-end request tracing.
 *
 * Required headers:
 * - traceparent: W3C trace context propagation
 * - tracestate: Additional vendor-specific trace data
 *
 * Spring Cloud Sleuth / Micrometer Tracing handles propagation
 * automatically via RestTemplate and WebClient interceptors.
 */
@Component
public class TracingContextPropagator {

    public static final String TRACE_PARENT_HEADER = "traceparent";
    public static final String TRACE_STATE_HEADER = "tracestate";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    /**
     * Extracts the trace ID from the traceparent header value.
     * Format: version-traceId-parentId-traceFlags (e.g., 00-abc123-def456-01)
     */
    public String extractTraceId(String traceparent) {
        if (traceparent == null || traceparent.isEmpty()) {
            return null;
        }
        String[] parts = traceparent.split("-");
        return parts.length >= 2 ? parts[1] : null;
    }
}
