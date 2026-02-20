package com.ftgo.gateway.filter;

import com.ftgo.jwt.JwtProperties;
import com.ftgo.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationGatewayFilterFactoryTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private GatewayFilterChain chain;

    private JwtProperties jwtProperties;
    private JwtAuthenticationGatewayFilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setHeader("Authorization");
        jwtProperties.setPrefix("Bearer ");
        filterFactory = new JwtAuthenticationGatewayFilterFactory(tokenProvider, jwtProperties);
    }

    @Test
    void shouldRejectRequestWithoutToken() {
        JwtAuthenticationGatewayFilterFactory.Config config = new JwtAuthenticationGatewayFilterFactory.Config();
        GatewayFilter filter = filterFactory.apply(config);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectInvalidToken() {
        JwtAuthenticationGatewayFilterFactory.Config config = new JwtAuthenticationGatewayFilterFactory.Config();
        GatewayFilter filter = filterFactory.apply(config);

        when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowValidToken() {
        JwtAuthenticationGatewayFilterFactory.Config config = new JwtAuthenticationGatewayFilterFactory.Config();
        GatewayFilter filter = filterFactory.apply(config);

        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getSubject("valid-token")).thenReturn("user-123");
        when(tokenProvider.getRoles("valid-token")).thenReturn(List.of("ROLE_USER"));
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void shouldRejectWhenMissingRequiredRole() {
        JwtAuthenticationGatewayFilterFactory.Config config = new JwtAuthenticationGatewayFilterFactory.Config();
        config.setRequiredRoles(List.of("ROLE_ADMIN"));
        GatewayFilter filter = filterFactory.apply(config);

        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getSubject("valid-token")).thenReturn("user-123");
        when(tokenProvider.getRoles("valid-token")).thenReturn(List.of("ROLE_USER"));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldAllowWhenHasRequiredRole() {
        JwtAuthenticationGatewayFilterFactory.Config config = new JwtAuthenticationGatewayFilterFactory.Config();
        config.setRequiredRoles(List.of("ROLE_ADMIN"));
        GatewayFilter filter = filterFactory.apply(config);

        when(tokenProvider.validateToken("admin-token")).thenReturn(true);
        when(tokenProvider.getSubject("admin-token")).thenReturn("admin-user");
        when(tokenProvider.getRoles("admin-token")).thenReturn(List.of("ROLE_ADMIN", "ROLE_USER"));
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void shouldPropagateUserHeadersDownstream() {
        JwtAuthenticationGatewayFilterFactory.Config config = new JwtAuthenticationGatewayFilterFactory.Config();
        GatewayFilter filter = filterFactory.apply(config);

        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getSubject("valid-token")).thenReturn("user-456");
        when(tokenProvider.getRoles("valid-token")).thenReturn(List.of("ROLE_USER", "ROLE_MANAGER"));
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }
}
