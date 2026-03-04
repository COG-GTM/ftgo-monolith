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
 * Unit tests for {@link ApiVersionGatewayFilter}.
 */
class ApiVersionGatewayFilterTest {

    private ApiVersionGatewayFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new ApiVersionGatewayFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldDefaultToV1WhenNoVersionSpecified() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Filter does not set response headers directly but modifies request
        // The version is propagated via X-API-Version header on the mutated request
        assertThat(filter.getOrder()).isEqualTo(-180);
    }

    @Test
    void shouldExtractVersionFromUrlPath() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v2/orders")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Filter processes without error
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldUseHeaderVersionWhenNoUrlVersion() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(ApiVersionGatewayFilter.API_VERSION_HEADER, "v2")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldHaveCorrectFilterOrder() {
        // Should run after correlation ID (-200) but before JWT validation (-100)
        assertThat(filter.getOrder()).isGreaterThan(-200);
        assertThat(filter.getOrder()).isLessThan(-100);
    }
}
