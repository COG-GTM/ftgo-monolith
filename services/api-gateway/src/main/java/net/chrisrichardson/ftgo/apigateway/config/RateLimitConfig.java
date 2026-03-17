package net.chrisrichardson.ftgo.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimitConfig {

    private static final String ANONYMOUS_KEY = "anonymous";

    /**
     * Resolves the rate limit key based on the API key header or client IP address.
     * If an X-API-Key header is present, it is used as the key.
     * Otherwise, the client's remote IP address is used.
     * Falls back to "anonymous" if the remote address cannot be determined (e.g., behind certain proxies).
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isBlank()) {
                return Mono.just(apiKey);
            }
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                return Mono.just(remoteAddress.getAddress().getHostAddress());
            }
            return Mono.just(ANONYMOUS_KEY);
        };
    }
}
