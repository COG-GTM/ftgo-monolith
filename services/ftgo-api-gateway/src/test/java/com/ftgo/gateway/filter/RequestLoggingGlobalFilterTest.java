package com.ftgo.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the request logging global filter.
 */
class RequestLoggingGlobalFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    private RequestLoggingGlobalFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
        filter = new RequestLoggingGlobalFilter();
    }

    @Test
    @DisplayName("Should execute without error for a normal request")
    void shouldLogRequestAndResponse() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header("X-Correlation-Id", "test-correlation-id")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should have correct ordering after correlation ID filter")
    void shouldHaveCorrectOrdering() {
        assertThat(filter.getOrder()).isEqualTo(Integer.MIN_VALUE + 2);
    }
}
