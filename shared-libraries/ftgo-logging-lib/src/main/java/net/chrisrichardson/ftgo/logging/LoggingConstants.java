package net.chrisrichardson.ftgo.logging;

/**
 * Constants used across the logging library for MDC keys and HTTP headers.
 */
public final class LoggingConstants {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_SERVICE_NAME = "serviceName";
    public static final String MDC_REQUEST_METHOD = "requestMethod";
    public static final String MDC_REQUEST_URI = "requestUri";
    public static final String MDC_CLIENT_IP = "clientIp";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";

    private LoggingConstants() {
    }
}
