package com.ftgo.common.error.handler;

/**
 * No-op implementation of {@link TraceIdProvider} that always returns null.
 *
 * <p>Used as a fallback when Micrometer Tracing is not on the classpath.</p>
 */
public class NoOpTraceIdProvider implements TraceIdProvider {

    @Override
    public String getTraceId() {
        return null;
    }
}
