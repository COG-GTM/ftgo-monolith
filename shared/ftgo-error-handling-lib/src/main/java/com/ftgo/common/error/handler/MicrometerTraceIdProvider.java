package com.ftgo.common.error.handler;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

/**
 * {@link TraceIdProvider} implementation that extracts trace IDs from
 * Micrometer Tracing's {@link Tracer}.
 *
 * <p>This is auto-configured when Micrometer Tracing is on the classpath,
 * integrating with the ftgo-tracing-lib for distributed tracing support.</p>
 */
public class MicrometerTraceIdProvider implements TraceIdProvider {

    private final Tracer tracer;

    public MicrometerTraceIdProvider(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public String getTraceId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null && currentSpan.context() != null) {
            return currentSpan.context().traceId();
        }
        return null;
    }
}
