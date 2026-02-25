package com.ftgo.common.logging.appender;

/**
 * Constants used across the FTGO logging library.
 *
 * <p>Defines standard MDC field names, log patterns, and configuration
 * defaults used for structured logging and ELK/EFK integration.</p>
 */
public final class LoggingConstants {

    private LoggingConstants() {
        // Utility class - prevent instantiation
    }

    // ---------------------------------------------------------------
    // MDC Field Names
    // ---------------------------------------------------------------

    /** MDC key for the distributed trace ID (from Micrometer Tracing). */
    public static final String MDC_TRACE_ID = "traceId";

    /** MDC key for the current span ID (from Micrometer Tracing). */
    public static final String MDC_SPAN_ID = "spanId";

    /** MDC key for the correlation ID (from API Gateway). */
    public static final String MDC_CORRELATION_ID = "correlationId";

    /** MDC key for the service/application name. */
    public static final String MDC_SERVICE_NAME = "service";

    /** MDC key for the HTTP request method. */
    public static final String MDC_REQUEST_METHOD = "requestMethod";

    /** MDC key for the request URI. */
    public static final String MDC_REQUEST_URI = "requestUri";

    /** MDC key for the remote client address. */
    public static final String MDC_REMOTE_ADDR = "remoteAddr";

    /** MDC key for the User-Agent header. */
    public static final String MDC_USER_AGENT = "userAgent";

    // ---------------------------------------------------------------
    // Default Header Names
    // ---------------------------------------------------------------

    /** Default HTTP header name for correlation ID. */
    public static final String DEFAULT_CORRELATION_HEADER = "X-Correlation-ID";

    /** HTTP header for forwarded-for proxy chain. */
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    // ---------------------------------------------------------------
    // Log Patterns
    // ---------------------------------------------------------------

    /**
     * Console log pattern with trace context for non-JSON output.
     * Includes timestamp, level, service name, traceId, spanId, correlationId,
     * thread, logger, and message.
     */
    public static final String CONSOLE_LOG_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} %5level [%X{service:-unknown},%X{traceId:-},%X{spanId:-},%X{correlationId:-}] "
                    + "[%thread] %logger{36} - %msg%n";

    /**
     * JSON log pattern identifier for Logback configuration.
     * When JSON is enabled, this pattern is replaced by LogstashEncoder output.
     */
    public static final String JSON_APPENDER_NAME = "JSON_CONSOLE";

    /** Name for the async wrapper appender. */
    public static final String ASYNC_APPENDER_NAME = "ASYNC_JSON";

    // ---------------------------------------------------------------
    // Elasticsearch / EFK Defaults
    // ---------------------------------------------------------------

    /** Default Elasticsearch host for log shipping. */
    public static final String DEFAULT_ES_HOST = "elasticsearch";

    /** Default Elasticsearch port. */
    public static final int DEFAULT_ES_PORT = 9200;

    /** Default index pattern for FTGO logs in Elasticsearch. */
    public static final String DEFAULT_ES_INDEX_PATTERN = "ftgo-logs-*";

    /** Default log retention for development (7 days). */
    public static final int RETENTION_DEV_DAYS = 7;

    /** Default log retention for staging (30 days). */
    public static final int RETENTION_STAGING_DAYS = 30;

    /** Default log retention for production (90 days). */
    public static final int RETENTION_PROD_DAYS = 90;
}
