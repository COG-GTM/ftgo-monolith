package com.ftgo.gateway.filter;

import com.ftgo.gateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JwtValidationGatewayFilter}.
 */
class JwtValidationGatewayFilterTest {

    private static final String SECRET = "this-is-a-test-secret-key-that-is-at-least-32-bytes-long-for-hs256";
    private static final String ISSUER = "ftgo-platform";

    private JwtValidationGatewayFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);
        properties.setEnabled(true);

        filter = new JwtValidationGatewayFilter(properties);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowPublicPathsWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // No 401 status set means the request passed through
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldRejectRequestsWithoutAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectRequestsWithInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectExpiredToken() {
        String expiredToken = generateToken("user-1", "testuser", List.of("ROLE_USER"),
                new Date(System.currentTimeMillis() - 60000)); // Expired 1 minute ago

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAcceptValidTokenAndForwardClaims() {
        String validToken = generateToken("user-123", "john.doe", List.of("ROLE_USER", "ROLE_ADMIN"),
                new Date(System.currentTimeMillis() + 3600000)); // Expires in 1 hour

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Should not set 401 status
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldSkipValidationWhenDisabled() {
        GatewayJwtProperties disabledProperties = new GatewayJwtProperties();
        disabledProperties.setEnabled(false);
        JwtValidationGatewayFilter disabledFilter = new JwtValidationGatewayFilter(disabledProperties);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        disabledFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldRejectNonAccessToken() {
        String refreshToken = generateRefreshToken("user-123");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String generateToken(String userId, String username, List<String> roles, Date expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userId)
                .claims(Map.of(
                        "username", username,
                        "roles", roles,
                        "type", "access"
                ))
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private String generateRefreshToken(String userId) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userId)
                .claims(Map.of("type", "refresh"))
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
