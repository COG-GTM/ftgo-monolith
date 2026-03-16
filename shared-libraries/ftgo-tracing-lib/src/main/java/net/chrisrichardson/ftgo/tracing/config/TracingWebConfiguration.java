package net.chrisrichardson.ftgo.tracing.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for HTTP trace context propagation.
 *
 * <p>Provides a {@link RestTemplate} bean that automatically propagates
 * B3 tracing headers when making outbound HTTP calls to other services.
 * Sleuth already instruments {@link RestTemplate} beans, so this configuration
 * primarily ensures a default bean is available for injection.</p>
 */
@Configuration
@ConditionalOnProperty(name = "ftgo.tracing.enabled", havingValue = "true", matchIfMissing = true)
public class TracingWebConfiguration {

    /**
     * Provides a RestTemplate bean that will be automatically instrumented
     * by Spring Cloud Sleuth for trace context propagation via B3 headers.
     *
     * <p>Sleuth intercepts all {@link RestTemplate} beans and injects
     * {@code X-B3-TraceId}, {@code X-B3-SpanId}, {@code X-B3-ParentSpanId},
     * and {@code X-B3-Sampled} headers on outbound requests.</p>
     */
    @Bean
    public RestTemplate tracingRestTemplate() {
        return new RestTemplate();
    }
}
