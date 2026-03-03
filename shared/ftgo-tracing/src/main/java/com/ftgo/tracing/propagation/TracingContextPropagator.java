package com.ftgo.tracing.propagation;

import com.ftgo.tracing.TracingConstants;
import com.ftgo.tracing.config.FtgoTracingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Manages trace context propagation across service boundaries.
 *
 * <p>This component is responsible for ensuring that trace context (traceId, spanId)
 * is properly propagated when making outgoing HTTP calls to downstream services.
 * It supports both B3 (Brave/Zipkin) and W3C Trace Context propagation formats.</p>
 *
 * <h3>Propagation Formats:</h3>
 * <ul>
 *   <li><strong>B3 (default)</strong>: Uses X-B3-TraceId, X-B3-SpanId, X-B3-ParentSpanId,
 *       X-B3-Sampled headers. Compatible with Zipkin and most tracing backends.</li>
 *   <li><strong>W3C</strong>: Uses traceparent and tracestate headers.
 *       Standard format for OpenTelemetry-based systems.</li>
 * </ul>
 *
 * <h3>Integration with Micrometer Tracing:</h3>
 * <p>Micrometer Tracing with the Brave bridge automatically handles propagation
 * for instrumented HTTP clients (RestTemplate, WebClient). This class provides
 * additional utilities for manual propagation in custom scenarios.</p>
 *
 * <h3>MDC Integration:</h3>
 * <p>Micrometer Tracing automatically populates the SLF4J MDC with traceId and
 * spanId fields. This class provides helper methods to access these values
 * for manual header injection when needed.</p>
 */
public class TracingContextPropagator {

    private static final Logger log = LoggerFactory.getLogger(TracingContextPropagator.class);

    private final FtgoTracingProperties properties;

    public TracingContextPropagator(FtgoTracingProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns the current trace ID from the MDC context, or null if not available.
     * The trace ID is automatically populated by Micrometer Tracing's Brave bridge.
     *
     * @return the current trace ID, or null if no active trace
     */
    public String getCurrentTraceId() {
        return MDC.get(TracingConstants.MDC_TRACE_ID);
    }

    /**
     * Returns the current span ID from the MDC context, or null if not available.
     * The span ID is automatically populated by Micrometer Tracing's Brave bridge.
     *
     * @return the current span ID, or null if no active span
     */
    public String getCurrentSpanId() {
        return MDC.get(TracingConstants.MDC_SPAN_ID);
    }

    /**
     * Returns the configured propagation type (B3 or W3C).
     *
     * @return the propagation type string
     */
    public String getPropagationType() {
        return properties.getPropagationType();
    }

    /**
     * Checks whether B3 propagation is configured.
     *
     * @return true if B3 propagation is active
     */
    public boolean isB3Propagation() {
        return "B3".equalsIgnoreCase(properties.getPropagationType());
    }

    /**
     * Checks whether W3C Trace Context propagation is configured.
     *
     * @return true if W3C propagation is active
     */
    public boolean isW3CPropagation() {
        return "W3C".equalsIgnoreCase(properties.getPropagationType());
    }

    /**
     * Logs the current trace context for debugging purposes.
     * Useful during development to verify trace propagation is working.
     */
    public void logCurrentContext() {
        String traceId = getCurrentTraceId();
        String spanId = getCurrentSpanId();
        if (traceId != null) {
            log.debug("Current trace context: traceId={}, spanId={}, propagation={}",
                    traceId, spanId, properties.getPropagationType());
        } else {
            log.debug("No active trace context found");
        }
    }
}
