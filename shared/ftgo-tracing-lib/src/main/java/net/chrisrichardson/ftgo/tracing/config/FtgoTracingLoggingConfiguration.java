package net.chrisrichardson.ftgo.tracing.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration that documents the logging pattern for trace context inclusion.
 *
 * <p>For traceId and spanId to appear in log lines, services must configure
 * their logging pattern to include the MDC fields. This is done by adding
 * {@code traceId} and {@code spanId} to the log pattern:
 *
 * <pre>
 * logging:
 *   pattern:
 *     level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
 * </pre>
 *
 * <p>The Brave MDCScopeDecorator (configured in {@link FtgoTracingAutoConfiguration})
 * automatically populates the MDC fields when a trace is active. These fields
 * are available in any logging framework that supports SLF4J MDC (Logback, Log4j2).
 *
 * <p>The recommended application.yml configuration for FTGO services:
 * <pre>
 * # Development (100% sampling)
 * ftgo:
 *   tracing:
 *     enabled: true
 *     sampling-probability: 1.0
 *     zipkin-endpoint: http://zipkin:9411/api/v2/spans
 *
 * logging:
 *   pattern:
 *     level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
 *
 * # Production (10% sampling)
 * ftgo:
 *   tracing:
 *     enabled: true
 *     sampling-probability: 0.1
 *     zipkin-endpoint: http://zipkin:9411/api/v2/spans
 * </pre>
 */
@Configuration
@ConditionalOnProperty(prefix = "ftgo.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FtgoTracingLoggingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoTracingLoggingConfiguration.class);

    /**
     * The recommended SLF4J logging pattern including trace context.
     * Services should use this pattern in their logging configuration
     * to include traceId and spanId in log output.
     */
    public static final String RECOMMENDED_LOG_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}] "
                    + "%c{1} - %m%n";
}
