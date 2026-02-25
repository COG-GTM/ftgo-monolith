package com.ftgo.common.tracing.config;

import brave.Tracing;
import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for FTGO distributed tracing.
 *
 * <p>Activates when Micrometer Tracing and Brave are on the classpath.
 * Provides sensible defaults for trace propagation, sampling, and
 * Zipkin/Jaeger reporting.</p>
 *
 * <p>This replaces the non-functional Spring Cloud Sleuth configuration
 * from the monolith with working Micrometer Tracing (Spring Boot 3.x).</p>
 *
 * <h3>Configuration Properties</h3>
 * <ul>
 *   <li>{@code ftgo.tracing.enabled} - Enable/disable tracing (default: true)</li>
 *   <li>{@code management.tracing.sampling.probability} - Sampling rate 0.0-1.0 (default: 1.0)</li>
 *   <li>{@code management.zipkin.tracing.endpoint} - Zipkin collector URL</li>
 * </ul>
 *
 * @see TracingLoggingConfiguration
 * @see TracePropagationConfiguration
 */
@Configuration
@ConditionalOnClass({Tracer.class, Tracing.class})
@ConditionalOnProperty(name = "ftgo.tracing.enabled", havingValue = "true", matchIfMissing = true)
@Import({TracingLoggingConfiguration.class, TracePropagationConfiguration.class})
public class FtgoTracingAutoConfiguration {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    /**
     * Provides a default Brave Sampler if none is configured.
     *
     * <p>The sampling probability is controlled by
     * {@code management.tracing.sampling.probability} (Spring Boot default).
     * This bean provides a fallback that samples all requests (1.0).</p>
     *
     * <p>Recommended sampling rates:</p>
     * <ul>
     *   <li>Development: 1.0 (100% - sample every request)</li>
     *   <li>Staging: 0.5 (50%)</li>
     *   <li>Production: 0.1 (10%)</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean(Sampler.class)
    public Sampler defaultSampler(
            @Value("${management.tracing.sampling.probability:1.0}") float probability) {
        if (probability >= 1.0f) {
            return Sampler.ALWAYS_SAMPLE;
        } else if (probability <= 0.0f) {
            return Sampler.NEVER_SAMPLE;
        }
        return Sampler.create(probability);
    }
}
