package com.ftgo.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class CorrelationIdGlobalFilterTest {

    @Mock
    private GatewayFilterChain chain;

    private CorrelationIdGlobalFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdGlobalFilter();
    }

    @Test
    void shouldGenerateCorrelationIdWhenNotPresent() {
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        ServerWebExchange capturedExchange = captor.getValue();
        String correlationId = capturedExchange.getRequest().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
        assertThat(correlationId).isNotNull().isNotEmpty();
    }

    @Test
    void shouldPreserveExistingCorrelationId() {
        when(chain.filter(any())).thenReturn(Mono.empty());

        String existingCorrelationId = "existing-correlation-id-123";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER, existingCorrelationId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        ServerWebExchange capturedExchange = captor.getValue();
        String correlationId = capturedExchange.getRequest().getHeaders()
                .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
        assertThat(correlationId).isEqualTo(existingCorrelationId);
    }

    @Test
    void shouldHaveHighestPrecedenceOrder() {
        assertThat(filter.getOrder()).isEqualTo(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
    }
}
