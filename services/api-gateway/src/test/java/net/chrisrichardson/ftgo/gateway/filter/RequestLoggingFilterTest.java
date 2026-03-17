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

class RequestLoggingFilterTest {

    private RequestLoggingFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldLogRequestAndResponse() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        verify(chain).filter(any());
    }

    @Test
    void shouldHaveCorrectOrder() {
        assertEquals(-150, filter.getOrder());
    }
}
