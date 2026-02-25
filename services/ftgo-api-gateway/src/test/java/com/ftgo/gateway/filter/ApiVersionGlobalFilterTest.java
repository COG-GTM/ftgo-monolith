package com.ftgo.gateway.filter;

import com.ftgo.gateway.config.GatewayProperties;
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
 * Unit tests for the API versioning global filter.
 */
class ApiVersionGlobalFilterTest {

    @Mock
    private GatewayFilterChain filterChain;

    private ApiVersionGlobalFilter filter;
    private GatewayProperties gatewayProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        gatewayProperties = new GatewayProperties();
        gatewayProperties.getVersioning().setEnabled(true);
        gatewayProperties.getVersioning().setHeaderName("X-API-Version");
        gatewayProperties.getVersioning().setDefaultVersion("v1");

        filter = new ApiVersionGlobalFilter(gatewayProperties);
    }

    @Test
    @DisplayName("Should add default version header when not present")
    void shouldAddDefaultVersionHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(exchangeCaptor.capture());

        String version = exchangeCaptor.getValue().getRequest().getHeaders().getFirst("X-API-Version");
        assertThat(version).isEqualTo("v1");
    }

    @Test
    @DisplayName("Should preserve existing version header")
    void shouldPreserveExistingVersionHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header("X-API-Version", "v2")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        // Filter chain should be called with the original exchange (not mutated)
        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Should skip versioning when disabled")
    void shouldSkipWhenDisabled() {
        gatewayProperties.getVersioning().setEnabled(false);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
    }
}
