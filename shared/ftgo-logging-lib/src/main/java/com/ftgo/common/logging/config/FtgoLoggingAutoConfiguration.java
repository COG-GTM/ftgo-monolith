package com.ftgo.common.logging.config;

import com.ftgo.common.logging.filter.CorrelationIdFilter;
import com.ftgo.common.logging.filter.MdcContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for FTGO centralized logging.
 *
 * <p>Activates when Logback is on the classpath and provides:</p>
 * <ul>
 *   <li>Structured JSON logging via logstash-logback-encoder</li>
 *   <li>Correlation ID propagation from API Gateway</li>
 *   <li>MDC context enrichment with service name, traceId, spanId</li>
 *   <li>Async appender configuration for non-blocking logging</li>
 * </ul>
 *
 * <h3>Configuration Properties</h3>
 * <ul>
 *   <li>{@code ftgo.logging.enabled} - Enable/disable logging library (default: true)</li>
 *   <li>{@code ftgo.logging.json.enabled} - Enable JSON format (default: true)</li>
 *   <li>{@code ftgo.logging.correlation-id.enabled} - Enable correlation ID (default: true)</li>
 *   <li>{@code ftgo.logging.async.enabled} - Enable async appender (default: true)</li>
 * </ul>
 *
 * @see LoggingProperties
 * @see CorrelationIdFilter
 * @see MdcContextFilter
 */
@Configuration
@ConditionalOnClass(ch.qos.logback.classic.LoggerContext.class)
@ConditionalOnProperty(name = "ftgo.logging.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LoggingProperties.class)
@Import(LogbackJsonConfiguration.class)
public class FtgoLoggingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoLoggingAutoConfiguration.class);

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    /**
     * Registers the correlation ID servlet filter for web applications.
     *
     * <p>This filter extracts the correlation ID from the incoming HTTP request
     * header (default: X-Correlation-ID) and places it in the MDC for inclusion
     * in all log entries. If no correlation ID is present, a new UUID is generated.</p>
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "ftgo.logging.correlation-id.enabled", havingValue = "true", matchIfMissing = true)
    public CorrelationIdFilter correlationIdFilter(LoggingProperties properties) {
        log.info("Registering CorrelationIdFilter with header: {}",
                properties.getCorrelationId().getHeaderName());
        return new CorrelationIdFilter(properties);
    }

    /**
     * Registers the MDC context filter for web applications.
     *
     * <p>This filter enriches the MDC with the service name and other
     * contextual information for every request.</p>
     */
    @Bean
    @ConditionalOnWebApplication
    public MdcContextFilter mdcContextFilter() {
        log.info("Registering MdcContextFilter for service: {}", applicationName);
        return new MdcContextFilter(applicationName);
    }
}
