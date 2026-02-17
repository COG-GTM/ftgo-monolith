package net.chrisrichardson.ftgo.observability.tracing;

import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Distributed tracing configuration for FTGO microservices.
 * Integrates Micrometer Tracing with Zipkin exporter for
 * end-to-end request tracing across services.
 */
@AutoConfiguration
@ConditionalOnClass(Tracer.class)
public class FtgoTracingConfiguration {

    @Bean
    public TracingContextPropagator tracingContextPropagator() {
        return new TracingContextPropagator();
    }
}
