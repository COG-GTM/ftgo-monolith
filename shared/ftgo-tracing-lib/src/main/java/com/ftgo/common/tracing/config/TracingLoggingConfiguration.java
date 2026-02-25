package com.ftgo.common.tracing.config;

import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Configures trace context propagation into log lines.
 *
 * <p>When Micrometer Tracing is active, Spring Boot 3.x automatically
 * adds traceId and spanId to the MDC (Mapped Diagnostic Context).
 * This configuration ensures the logging pattern includes these fields.</p>
 *
 * <h3>Default Log Pattern</h3>
 * <p>Spring Boot 3.x with Micrometer Tracing automatically includes
 * trace context in logs when using the default logging configuration.
 * The following application properties can customize the pattern:</p>
 *
 * <pre>
 * logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
 * </pre>
 *
 * <h3>Log Output Example</h3>
 * <pre>
 * INFO [ftgo-order-service,64b8f2e3d1a7c5b0,64b8f2e3d1a7c5b0] - Processing order 12345
 * </pre>
 *
 * <p>The traceId and spanId are automatically managed by the Brave bridge
 * and placed into the SLF4J MDC context.</p>
 */
@Configuration
@ConditionalOnClass(Tracer.class)
public class TracingLoggingConfiguration {

    /**
     * The default logging pattern that includes trace context.
     * Services can reference this constant in their logging configuration.
     */
    public static final String TRACE_LOG_PATTERN =
            "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]";

    /**
     * The recommended Logback pattern for FTGO services with tracing.
     * Includes timestamp, log level, trace context, logger, and message.
     */
    public static final String FULL_LOG_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}] "
                    + "%-40.40logger{39} : %m%n";
}
