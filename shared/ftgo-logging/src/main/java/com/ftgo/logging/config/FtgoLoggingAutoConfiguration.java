package com.ftgo.logging.config;

import com.ftgo.logging.filter.CorrelationIdFilter;
import com.ftgo.logging.filter.ServiceNameInitializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for FTGO structured logging.
 *
 * <p>Automatically registers the following beans when the library is on the classpath:</p>
 * <ul>
 *   <li>{@link CorrelationIdFilter} - Populates MDC with correlation ID and request context</li>
 *   <li>{@link ServiceNameInitializer} - Sets the service name in MDC from application properties</li>
 * </ul>
 *
 * <p>Services only need to add this library as a dependency and set
 * {@code spring.application.name} in their configuration. The logging
 * infrastructure is configured automatically via the included
 * {@code logback-spring.xml} on the classpath.</p>
 *
 * <p>To customize logging behavior, services can:</p>
 * <ul>
 *   <li>Override the Logback config by providing their own {@code logback-spring.xml}</li>
 *   <li>Set {@code ftgo.logging.json.enabled=false} to disable JSON output</li>
 *   <li>Provide their own {@link CorrelationIdFilter} bean to customize correlation ID handling</li>
 * </ul>
 */
@Configuration
public class FtgoLoggingAutoConfiguration {

    /**
     * Registers the correlation ID filter for web applications.
     * This filter populates the MDC with correlation ID, request ID,
     * HTTP method, URI, and client IP for every incoming request.
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnMissingBean(CorrelationIdFilter.class)
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    /**
     * Registers the service name initializer that populates MDC
     * with the service name from {@code spring.application.name}.
     */
    @Bean
    @ConditionalOnMissingBean(ServiceNameInitializer.class)
    public ServiceNameInitializer serviceNameInitializer() {
        return new ServiceNameInitializer("unknown-service");
    }
}
