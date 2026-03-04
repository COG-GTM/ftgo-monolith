package com.ftgo.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RequestLoggingGatewayFilter}.
 */
class RequestLoggingGatewayFilterTest {

    private RequestLoggingGatewayFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingGatewayFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldLogRequestAndResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header("X-Correlation-ID", "test-correlation-id")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Filter should complete without error
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldHandleMissingCorrelationId() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldHaveCorrectFilterOrder() {
        // Should run after correlation ID (-200) but before JWT (-100)
        assertThat(filter.getOrder()).isEqualTo(-150);
    }
}
