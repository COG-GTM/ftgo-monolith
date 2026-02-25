package com.ftgo.gateway.ratelimit;

import com.ftgo.gateway.config.GatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the in-memory rate limiter.
 */
class InMemoryRateLimiterTest {

    private InMemoryRateLimiter rateLimiter;
    private GatewayProperties gatewayProperties;

    @BeforeEach
    void setUp() {
        gatewayProperties = new GatewayProperties();
        gatewayProperties.getRateLimit().setEnabled(true);
        gatewayProperties.getRateLimit().setDefaultReplenishRate(5);
        gatewayProperties.getRateLimit().setDefaultBurstCapacity(10);
        rateLimiter = new InMemoryRateLimiter(gatewayProperties);
    }

    @Test
    @DisplayName("Should allow requests within rate limit")
    void shouldAllowRequestsWithinLimit() {
        StepVerifier.create(rateLimiter.isAllowed("order-service", "client-1"))
                .assertNext(response -> {
                    assertThat(response.isAllowed()).isTrue();
                    assertThat(response.getHeaders()).containsKey("X-RateLimit-Remaining");
                    assertThat(response.getHeaders()).containsKey("X-RateLimit-Burst-Capacity");
                    assertThat(response.getHeaders()).containsKey("X-RateLimit-Replenish-Rate");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should reject requests exceeding burst capacity")
    void shouldRejectRequestsExceedingBurstCapacity() {
        // Consume all tokens in the bucket
        for (int i = 0; i < 10; i++) {
            rateLimiter.isAllowed("order-service", "client-2").block();
        }

        // Next request should be rejected
        StepVerifier.create(rateLimiter.isAllowed("order-service", "client-2"))
                .assertNext(response -> assertThat(response.isAllowed()).isFalse())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should use separate buckets for different clients")
    void shouldUseSeparateBucketsPerClient() {
        // Exhaust tokens for client-a
        for (int i = 0; i < 10; i++) {
            rateLimiter.isAllowed("order-service", "client-a").block();
        }

        // client-b should still have tokens
        StepVerifier.create(rateLimiter.isAllowed("order-service", "client-b"))
                .assertNext(response -> assertThat(response.isAllowed()).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should allow all requests when rate limiting is disabled")
    void shouldAllowAllWhenDisabled() {
        gatewayProperties.getRateLimit().setEnabled(false);

        StepVerifier.create(rateLimiter.isAllowed("order-service", "any-client"))
                .assertNext(response -> assertThat(response.isAllowed()).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should use endpoint-specific configuration when available")
    void shouldUseEndpointSpecificConfig() {
        GatewayProperties.EndpointRateLimit endpointConfig = new GatewayProperties.EndpointRateLimit();
        endpointConfig.setReplenishRate(2);
        endpointConfig.setBurstCapacity(3);
        gatewayProperties.getRateLimit().getEndpoints().put("special-route", endpointConfig);

        // Exhaust the 3-token bucket
        for (int i = 0; i < 3; i++) {
            RateLimiter.Response response = rateLimiter.isAllowed("special-route", "client-x").block();
            assertThat(response).isNotNull();
            assertThat(response.isAllowed()).isTrue();
        }

        // 4th request should be rejected
        StepVerifier.create(rateLimiter.isAllowed("special-route", "client-x"))
                .assertNext(response -> assertThat(response.isAllowed()).isFalse())
                .verifyComplete();
    }
}
