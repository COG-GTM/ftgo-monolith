package com.ftgo.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestLoggingGlobalFilterTest {

    @Mock
    private GatewayFilterChain chain;

    private RequestLoggingGlobalFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingGlobalFilter();
    }

    @Test
    void shouldLogRequestAndResponse() {
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header("X-Correlation-Id", "test-correlation-id")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void shouldSetStartTimeAttribute() {
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        Long startTime = exchange.getAttribute("gateway.request.startTime");
        assertThat(startTime).isNotNull().isPositive();
    }

    @Test
    void shouldHaveCorrectOrder() {
        assertThat(filter.getOrder()).isEqualTo(org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 1);
    }
}
