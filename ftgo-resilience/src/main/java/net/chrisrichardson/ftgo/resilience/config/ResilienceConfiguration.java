package net.chrisrichardson.ftgo.resilience.config;

import net.chrisrichardson.ftgo.resilience.circuitbreaker.CircuitBreakerConfiguration;
import net.chrisrichardson.ftgo.resilience.retry.RetryConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CircuitBreakerConfiguration.class, RetryConfiguration.class})
public class ResilienceConfiguration {
}
