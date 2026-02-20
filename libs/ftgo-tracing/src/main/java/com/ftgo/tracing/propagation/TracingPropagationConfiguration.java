package com.ftgo.tracing.propagation;

import com.ftgo.tracing.config.TracingProperties;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ContextPropagators.class)
public class TracingPropagationConfiguration {

    @Bean
    @ConditionalOnMissingBean(TextMapPropagator.class)
    TextMapPropagator textMapPropagator(TracingProperties properties) {
        return switch (properties.getPropagation().getType()) {
            case B3 -> B3Propagator.injectingSingleHeader();
            case B3_MULTI -> B3Propagator.injectingMultiHeaders();
            case W3C -> W3CTraceContextPropagator.getInstance();
        };
    }
}
