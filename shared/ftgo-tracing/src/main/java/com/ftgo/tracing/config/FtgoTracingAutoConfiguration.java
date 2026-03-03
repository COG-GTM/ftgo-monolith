package com.ftgo.tracing.config;

import com.ftgo.tracing.propagation.TracingContextPropagator;
import com.ftgo.tracing.span.BusinessSpanCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for FTGO distributed tracing.
 *
 * <p>Automatically configures the following when the library is on the classpath:</p>
 * <ul>
 *   <li>{@link FtgoTracingProperties} - Tracing configuration properties</li>
 *   <li>{@link TracingContextPropagator} - Propagates trace context to downstream calls</li>
 *   <li>{@link BusinessSpanCreator} - Creates custom spans for business operations</li>
 * </ul>
 *
 * <p>Services only need to add this library as a dependency and configure the
 * following properties:</p>
 * <pre>
 * ftgo.tracing.enabled=true
 * ftgo.tracing.zipkin-endpoint=http://zipkin:9411/api/v2/spans
 * ftgo.tracing.sampling-probability=1.0
 * </pre>
 *
 * <p>The tracing infrastructure integrates with the existing FTGO logging library
 * to include traceId and spanId in all log entries via the SLF4J MDC.</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "ftgo.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FtgoTracingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FtgoTracingAutoConfiguration.class);

    /**
     * Registers tracing configuration properties bean.
     */
    @Bean
    @ConditionalOnMissingBean(FtgoTracingProperties.class)
    public FtgoTracingProperties ftgoTracingProperties(
            @Value("${ftgo.tracing.enabled:true}") boolean enabled,
            @Value("${ftgo.tracing.sampling-probability:1.0}") float samplingProbability,
            @Value("${ftgo.tracing.zipkin-endpoint:http://localhost:9411/api/v2/spans}") String zipkinEndpoint,
            @Value("${ftgo.tracing.propagation-type:B3}") String propagationType,
            @Value("${spring.application.name:unknown-service}") String serviceName) {

        FtgoTracingProperties properties = new FtgoTracingProperties();
        properties.setEnabled(enabled);
        properties.setSamplingProbability(samplingProbability);
        properties.setZipkinEndpoint(zipkinEndpoint);
        properties.setPropagationType(propagationType);
        properties.setServiceName(serviceName);

        log.info("FTGO Tracing configured: enabled={}, samplingProbability={}, " +
                        "zipkinEndpoint={}, propagationType={}, serviceName={}",
                enabled, samplingProbability, zipkinEndpoint, propagationType, serviceName);

        return properties;
    }

    /**
     * Registers the trace context propagator for web applications.
     * This component ensures trace context headers are propagated
     * on outgoing HTTP requests.
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnMissingBean(TracingContextPropagator.class)
    public TracingContextPropagator tracingContextPropagator(FtgoTracingProperties properties) {
        log.info("Registering TracingContextPropagator with propagation type: {}",
                properties.getPropagationType());
        return new TracingContextPropagator(properties);
    }

    /**
     * Registers the business span creator for creating custom spans
     * around key business operations.
     */
    @Bean
    @ConditionalOnMissingBean(BusinessSpanCreator.class)
    public BusinessSpanCreator businessSpanCreator(FtgoTracingProperties properties) {
        log.info("Registering BusinessSpanCreator for service: {}", properties.getServiceName());
        return new BusinessSpanCreator(properties);
    }
}
