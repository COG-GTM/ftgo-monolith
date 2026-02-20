package com.ftgo.tracing.config;

import com.ftgo.tracing.exporter.TracingExporterConfiguration;
import com.ftgo.tracing.propagation.TracingPropagationConfiguration;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass({Tracer.class, ObservationRegistry.class})
@ConditionalOnProperty(prefix = "ftgo.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TracingProperties.class)
@Import({TracingExporterConfiguration.class, TracingPropagationConfiguration.class})
public class TracingAutoConfiguration {
}
