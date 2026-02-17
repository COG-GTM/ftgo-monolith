package net.chrisrichardson.ftgo.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnBean(MeterRegistry.class)
public class FtgoBusinessMetricsConfiguration {

    @Bean
    @ConditionalOnProperty(name = "ftgo.metrics.order.enabled", matchIfMissing = true)
    public OrderMetrics orderMetrics(MeterRegistry registry) {
        return new OrderMetrics(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.metrics.consumer.enabled", matchIfMissing = true)
    public ConsumerMetrics consumerMetrics(MeterRegistry registry) {
        return new ConsumerMetrics(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.metrics.restaurant.enabled", matchIfMissing = true)
    public RestaurantMetrics restaurantMetrics(MeterRegistry registry) {
        return new RestaurantMetrics(registry);
    }

    @Bean
    @ConditionalOnProperty(name = "ftgo.metrics.courier.enabled", matchIfMissing = true)
    public CourierMetrics courierMetrics(MeterRegistry registry) {
        return new CourierMetrics(registry);
    }
}
