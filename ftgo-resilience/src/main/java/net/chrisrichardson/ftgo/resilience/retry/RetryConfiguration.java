package net.chrisrichardson.ftgo.resilience.retry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
public class RetryConfiguration {

    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(IOException.class, TimeoutException.class)
                .build();
    }

    @Bean
    public RetryRegistry retryRegistry(RetryConfig defaultRetryConfig) {
        return RetryRegistry.of(defaultRetryConfig);
    }

    @Bean
    public Retry consumerServiceRetry(RetryRegistry registry) {
        return registry.retry("consumerService");
    }

    @Bean
    public Retry restaurantServiceRetry(RetryRegistry registry) {
        return registry.retry("restaurantService");
    }

    @Bean
    public Retry deliveryServiceRetry(RetryRegistry registry) {
        return registry.retry("deliveryService");
    }
}
