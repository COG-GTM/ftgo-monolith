package com.ftgo.logging;

/**
 * Constants used throughout the FTGO logging framework.
 * Defines standard MDC keys, HTTP header names, and log field names
 * for consistent structured logging across all microservices.
 */
public final class LoggingConstants {

    private LoggingConstants() {
        // Utility class - prevent instantiation
    }

    // -------------------------------------------------------------------------
    // MDC Keys - Used to populate structured log fields via SLF4J MDC
    // -------------------------------------------------------------------------

    /** MDC key for the correlation ID that links related requests across services. */
    public static final String MDC_CORRELATION_ID = "correlationId";

    /** MDC key for the authenticated user identifier. */
    public static final String MDC_USER_ID = "userId";

    /** MDC key for the distributed trace ID (e.g., from Brave/Zipkin). */
    public static final String MDC_TRACE_ID = "traceId";

    /** MDC key for the distributed span ID within a trace. */
    public static final String MDC_SPAN_ID = "spanId";

    /** MDC key for the service name producing the log entry. */
    public static final String MDC_SERVICE_NAME = "serviceName";

    /** MDC key for the HTTP method of the incoming request. */
    public static final String MDC_REQUEST_METHOD = "requestMethod";

    /** MDC key for the URI path of the incoming request. */
    public static final String MDC_REQUEST_URI = "requestUri";

    /** MDC key for the unique request ID (per-request, not cross-service). */
    public static final String MDC_REQUEST_ID = "requestId";

    /** MDC key for the client IP address. */
    public static final String MDC_CLIENT_IP = "clientIp";

    // -------------------------------------------------------------------------
    // HTTP Headers - Used to propagate context across service boundaries
    // -------------------------------------------------------------------------

    /** HTTP header for propagating the correlation ID between services. */
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    /** HTTP header for propagating the request ID between services. */
    public static final String HEADER_REQUEST_ID = "X-Request-ID";

    // -------------------------------------------------------------------------
    // Log Field Names - Standard field names in structured JSON output
    // -------------------------------------------------------------------------

    /** JSON field name for the log timestamp. */
    public static final String FIELD_TIMESTAMP = "@timestamp";

    /** JSON field name for the log level. */
    public static final String FIELD_LEVEL = "level";

    /** JSON field name for the logger name. */
    public static final String FIELD_LOGGER = "logger";

    /** JSON field name for the log message. */
    public static final String FIELD_MESSAGE = "message";

    /** JSON field name for the service name. */
    public static final String FIELD_SERVICE = "service";

    /** JSON field name for the correlation ID. */
    public static final String FIELD_CORRELATION_ID = "correlationId";

    /** JSON field name for the trace ID. */
    public static final String FIELD_TRACE_ID = "traceId";

    /** JSON field name for the span ID. */
    public static final String FIELD_SPAN_ID = "spanId";

    /** JSON field name for the user ID. */
    public static final String FIELD_USER_ID = "userId";

    // -------------------------------------------------------------------------
    // Environment Names - Used for log retention policy configuration
    // -------------------------------------------------------------------------

    public static final String ENV_DEVELOPMENT = "development";
    public static final String ENV_STAGING = "staging";
    public static final String ENV_PRODUCTION = "production";
}
