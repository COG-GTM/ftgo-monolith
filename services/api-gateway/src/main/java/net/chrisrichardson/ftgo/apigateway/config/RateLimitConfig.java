package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimitConfig {

    /**
     * Resolves the rate limit key based on the API key header or client IP address.
     * If an X-API-Key header is present, it is used as the key.
     * Otherwise, the client's remote IP address is used.
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isBlank()) {
                return Mono.just(apiKey);
            }
            return Mono.just(
                    Objects.requireNonNull(exchange.getRequest().getRemoteAddress(),
                            "Remote address must not be null").getAddress().getHostAddress()
            );
        };
    }
}
