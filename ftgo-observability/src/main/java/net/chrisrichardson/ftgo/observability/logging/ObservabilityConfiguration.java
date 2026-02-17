package net.chrisrichardson.ftgo.observability.logging;

import net.chrisrichardson.ftgo.observability.metrics.MicroserviceMetricsConfiguration;
import net.chrisrichardson.ftgo.observability.tracing.TracingConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TracingConfiguration.class, MicroserviceMetricsConfiguration.class})
public class ObservabilityConfiguration {
}
