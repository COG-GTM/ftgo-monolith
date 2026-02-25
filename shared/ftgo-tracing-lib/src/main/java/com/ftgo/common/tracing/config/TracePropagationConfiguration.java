package com.ftgo.common.tracing.config;

import brave.Tracing;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures trace context propagation across service boundaries.
 *
 * <p>Ensures trace context (traceId, spanId, parentSpanId) is propagated
 * via HTTP headers when services communicate with each other.</p>
 *
 * <h3>Supported Propagation Formats</h3>
 * <ul>
 *   <li><strong>B3</strong> (default) - Used by Zipkin ecosystem.
 *       Propagates via {@code X-B3-TraceId}, {@code X-B3-SpanId},
 *       {@code X-B3-ParentSpanId}, {@code X-B3-Sampled} headers.</li>
 *   <li><strong>W3C Trace Context</strong> - Can be enabled via
 *       {@code management.tracing.propagation.type=w3c}.
 *       Propagates via {@code traceparent} and {@code tracestate} headers.</li>
 * </ul>
 *
 * <h3>Configuration Properties</h3>
 * <pre>
 * # Propagation type (b3, b3_multi, w3c)
 * management.tracing.propagation.type=b3
 *
 * # For API Gateway pass-through
 * management.tracing.propagation.consume=b3,w3c
 * management.tracing.propagation.produce=b3
 * </pre>
 *
 * <h3>RestTemplate Integration</h3>
 * <p>Spring Boot 3.x automatically instruments {@code RestTemplate} and
 * {@code WebClient} when Micrometer Tracing is on the classpath.
 * Services using {@code RestTemplateBuilder} will automatically
 * propagate trace context.</p>
 */
@Configuration
@ConditionalOnClass({Tracer.class, Tracing.class})
public class TracePropagationConfiguration {

    /**
     * Customizes RestTemplate instances to log the service name for
     * debugging trace propagation issues.
     *
     * <p>Note: Actual trace header injection is handled automatically
     * by Spring Boot's Micrometer Tracing auto-configuration.</p>
     */
    @Bean
    public RestTemplateCustomizer ftgoTracingRestTemplateCustomizer() {
        return restTemplate -> {
            // Spring Boot 3.x with Micrometer Tracing automatically
            // adds trace propagation interceptors to RestTemplate.
            // This customizer is a hook for additional FTGO-specific
            // configuration if needed in the future.
        };
    }
}
