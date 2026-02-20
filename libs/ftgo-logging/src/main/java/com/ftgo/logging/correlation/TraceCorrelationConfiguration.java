package com.ftgo.logging.correlation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "ftgo.logging", name = "trace-correlation-enabled", havingValue = "true", matchIfMissing = true)
public class TraceCorrelationConfiguration {

    @Bean
    @ConditionalOnClass(name = "io.micrometer.tracing.Tracer")
    public TraceMdcLifecycleHandler traceMdcLifecycleHandler() {
        return new TraceMdcLifecycleHandler();
    }

    @Bean
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }
}
