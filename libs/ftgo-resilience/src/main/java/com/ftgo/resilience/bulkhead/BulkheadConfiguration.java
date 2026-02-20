package com.ftgo.resilience.bulkhead;

import com.ftgo.resilience.config.ResilienceProperties;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnClass(Bulkhead.class)
@ConditionalOnProperty(prefix = "ftgo.resilience.bulkhead", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BulkheadConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BulkheadConfig defaultBulkheadConfig(ResilienceProperties properties) {
        ResilienceProperties.Bulkhead bulkheadProps = properties.getBulkhead();
        return BulkheadConfig.custom()
                .maxConcurrentCalls(bulkheadProps.getMaxConcurrentCalls())
                .maxWaitDuration(Duration.ofMillis(bulkheadProps.getMaxWaitDurationMs()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public BulkheadRegistry bulkheadRegistry(BulkheadConfig config) {
        return BulkheadRegistry.of(config);
    }

    @Bean
    @ConditionalOnMissingBean(name = "bulkheadMetrics")
    @ConditionalOnBean(MeterRegistry.class)
    public TaggedBulkheadMetrics bulkheadMetrics(
            BulkheadRegistry registry,
            MeterRegistry meterRegistry) {
        TaggedBulkheadMetrics metrics = TaggedBulkheadMetrics.ofBulkheadRegistry(registry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean(name = "orderServiceBulkhead")
    public Bulkhead orderServiceBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("orderService");
    }

    @Bean(name = "restaurantServiceBulkhead")
    public Bulkhead restaurantServiceBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("restaurantService");
    }

    @Bean(name = "consumerServiceBulkhead")
    public Bulkhead consumerServiceBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("consumerService");
    }

    @Bean(name = "courierServiceBulkhead")
    public Bulkhead courierServiceBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("courierService");
    }

    @Bean(name = "externalPaymentBulkhead")
    public Bulkhead externalPaymentBulkhead(BulkheadRegistry registry) {
        BulkheadConfig externalConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();
        return registry.bulkhead("externalPayment", externalConfig);
    }
}
