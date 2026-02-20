package com.ftgo.observability.config;

import com.ftgo.observability.metrics.ConsumerMetrics;
import com.ftgo.observability.metrics.CourierMetrics;
import com.ftgo.observability.metrics.OrderMetrics;
import com.ftgo.observability.metrics.RestaurantMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = PrometheusMetricsAutoConfiguration.class)
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnProperty(prefix = "ftgo.observability.metrics", name = "business-metrics-enabled", havingValue = "true", matchIfMissing = true)
public class BusinessMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ftgo.observability.metrics", name = "order-metrics-enabled", havingValue = "true", matchIfMissing = true)
    public OrderMetrics orderMetrics(MeterRegistry registry) {
        return new OrderMetrics(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ftgo.observability.metrics", name = "consumer-metrics-enabled", havingValue = "true", matchIfMissing = true)
    public ConsumerMetrics consumerMetrics(MeterRegistry registry) {
        return new ConsumerMetrics(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ftgo.observability.metrics", name = "courier-metrics-enabled", havingValue = "true", matchIfMissing = true)
    public CourierMetrics courierMetrics(MeterRegistry registry) {
        return new CourierMetrics(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ftgo.observability.metrics", name = "restaurant-metrics-enabled", havingValue = "true", matchIfMissing = true)
    public RestaurantMetrics restaurantMetrics(MeterRegistry registry) {
        return new RestaurantMetrics(registry);
    }
}
