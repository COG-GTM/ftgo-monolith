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
 * Unit tests for {@link CorrelationIdGatewayFilter}.
 */
class CorrelationIdGatewayFilterTest {

    private CorrelationIdGatewayFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdGatewayFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldGenerateCorrelationIdWhenNotPresent() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Correlation ID should be added to the response
        String responseCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGatewayFilter.CORRELATION_ID_HEADER);
        assertThat(responseCorrelationId).isNotNull().isNotBlank();

        // Should be a valid UUID format
        assertThat(responseCorrelationId).matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void shouldReuseExistingCorrelationId() {
        String existingId = "existing-correlation-id-123";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(CorrelationIdGatewayFilter.CORRELATION_ID_HEADER, existingId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Should reuse the existing correlation ID in the response
        String responseCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGatewayFilter.CORRELATION_ID_HEADER);
        assertThat(responseCorrelationId).isEqualTo(existingId);
    }

    @Test
    void shouldStoreCorrelationIdInExchangeAttributes() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Correlation ID should be stored in exchange attributes
        String attrCorrelationId = exchange.getAttribute(CorrelationIdGatewayFilter.CORRELATION_ID_ATTR);
        assertThat(attrCorrelationId).isNotNull().isNotBlank();

        // Should match the response header
        String responseCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGatewayFilter.CORRELATION_ID_HEADER);
        assertThat(attrCorrelationId).isEqualTo(responseCorrelationId);
    }

    @Test
    void shouldHaveHigherPriorityThanJwtFilter() {
        assertThat(filter.getOrder()).isLessThan(-100); // JWT filter order is -100
    }
}
