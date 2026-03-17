package net.chrisrichardson.ftgo.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldGenerateCorrelationIdWhenNotPresent() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        String correlationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());

        String requestId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdFilter.REQUEST_ID_HEADER);
        assertNotNull(requestId);
    }

    @Test
    void shouldPropagateExistingCorrelationId() {
        String existingCorrelationId = "existing-correlation-id-123";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, existingCorrelationId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        String correlationId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertEquals(existingCorrelationId, correlationId);
    }

    @Test
    void shouldAlwaysGenerateRequestId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        String requestId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdFilter.REQUEST_ID_HEADER);
        assertNotNull(requestId);
        assertFalse(requestId.isEmpty());
    }

    @Test
    void shouldHaveCorrectOrder() {
        assertEquals(-200, filter.getOrder());
    }
}
