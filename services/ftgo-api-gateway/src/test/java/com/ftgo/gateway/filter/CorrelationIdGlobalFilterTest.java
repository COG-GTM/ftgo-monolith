package com.ftgo.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the Correlation ID global filter.
 */
class CorrelationIdGlobalFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    private CorrelationIdGlobalFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
        filter = new CorrelationIdGlobalFilter();
    }

    @Test
    @DisplayName("Should generate correlation ID when not present in request")
    void shouldGenerateCorrelationId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        // Verify correlation ID was set on the response
        String responseCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
        assertThat(responseCorrelationId).isNotNull().isNotBlank();

        // Verify it's a valid UUID format
        assertThat(responseCorrelationId).matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("Should propagate existing correlation ID from request")
    void shouldPropagateExistingCorrelationId() {
        String existingId = "existing-correlation-id-12345";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER, existingId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        // Verify the existing correlation ID was propagated to the response
        String responseCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
        assertThat(responseCorrelationId).isEqualTo(existingId);
    }

    @Test
    @DisplayName("Should forward correlation ID to downstream service")
    void shouldForwardCorrelationIdDownstream() {
        String existingId = "forward-correlation-id-67890";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER, existingId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        // Verify the filter chain was called with the mutated exchange
        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(exchangeCaptor.capture());

        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        String forwardedId = capturedExchange.getRequest().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
        assertThat(forwardedId).isEqualTo(existingId);
    }

    @Test
    @DisplayName("Should have highest precedence ordering")
    void shouldHaveHighestPrecedenceOrdering() {
        assertThat(filter.getOrder()).isEqualTo(Integer.MIN_VALUE + 1);
    }
}
