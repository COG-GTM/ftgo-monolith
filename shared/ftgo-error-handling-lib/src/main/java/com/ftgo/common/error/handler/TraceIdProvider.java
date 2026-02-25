package com.ftgo.common.error.handler;

/**
 * Strategy interface for providing trace IDs in error responses.
 *
 * <p>The default implementation returns null (no trace ID).
 * When Micrometer Tracing is on the classpath, the
 * {@link MicrometerTraceIdProvider} is used instead.</p>
 *
 * @see MicrometerTraceIdProvider
 * @see NoOpTraceIdProvider
 */
public interface TraceIdProvider {

    /**
     * Returns the current trace ID, or {@code null} if not available.
     *
     * @return the trace ID string, or null
     */
    String getTraceId();
}
