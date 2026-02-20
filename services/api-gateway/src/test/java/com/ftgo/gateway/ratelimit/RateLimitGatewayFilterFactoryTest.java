package com.ftgo.gateway.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitGatewayFilterFactoryTest {

    @Mock
    private GatewayFilterChain chain;

    private RateLimitGatewayFilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        filterFactory = new RateLimitGatewayFilterFactory();
    }

    @Test
    void shouldAllowRequestWithinLimit() {
        RateLimitGatewayFilterFactory.Config config = new RateLimitGatewayFilterFactory.Config();
        config.setRequestsPerSecond(10);
        config.setBurstCapacity(20);
        GatewayFilter filter = filterFactory.apply(config);

        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining")).isNotNull();
        assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("20");
    }

    @Test
    void shouldRejectRequestWhenRateLimitExceeded() {
        RateLimitGatewayFilterFactory.Config config = new RateLimitGatewayFilterFactory.Config();
        config.setRequestsPerSecond(1);
        config.setBurstCapacity(1);
        GatewayFilter filter = filterFactory.apply(config);

        when(chain.filter(any())).thenReturn(Mono.empty());

        for (int i = 0; i < 2; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test-ratelimit").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            filter.filter(exchange, chain).block();

            if (i == 1) {
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            }
        }
    }

    @Test
    void shouldAddRateLimitHeaders() {
        RateLimitGatewayFilterFactory.Config config = new RateLimitGatewayFilterFactory.Config();
        config.setRequestsPerSecond(100);
        config.setBurstCapacity(200);
        GatewayFilter filter = filterFactory.apply(config);

        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders-headers").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getHeaders().containsKey("X-RateLimit-Remaining")).isTrue();
        assertThat(exchange.getResponse().getHeaders().containsKey("X-RateLimit-Limit")).isTrue();
    }
}
