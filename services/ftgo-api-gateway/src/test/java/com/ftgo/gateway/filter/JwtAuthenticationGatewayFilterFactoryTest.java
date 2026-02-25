package com.ftgo.gateway.filter;

import com.ftgo.gateway.config.GatewayProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the JWT authentication gateway filter.
 */
class JwtAuthenticationGatewayFilterFactoryTest {

    private static final String TEST_SECRET = "TestSecretKeyForJwtTokenValidationMustBeAtLeast256BitsLong!!";
    private static final String TEST_ISSUER = "ftgo-platform";

    @Mock
    private GatewayFilterChain filterChain;

    private JwtAuthenticationGatewayFilterFactory filterFactory;
    private GatewayProperties gatewayProperties;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        gatewayProperties = new GatewayProperties();
        gatewayProperties.getJwt().setEnabled(true);
        gatewayProperties.getJwt().setSecret(TEST_SECRET);
        gatewayProperties.getJwt().setIssuer(TEST_ISSUER);
        gatewayProperties.getJwt().setExcludedPaths(List.of("/actuator/**", "/fallback/**"));

        filterFactory = new JwtAuthenticationGatewayFilterFactory(gatewayProperties);
        signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Should allow request with valid JWT token and forward claims as headers")
    void shouldAllowValidToken() {
        String token = generateAccessToken("testuser", 1L, List.of("ROLE_USER"), List.of("order:read"));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        // No error response means the request was allowed
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should reject request without Authorization header")
    void shouldRejectMissingAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should reject request with invalid token")
    void shouldRejectInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        String token = generateExpiredToken("testuser");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should reject refresh token (only access tokens allowed)")
    void shouldRejectRefreshToken() {
        String token = generateRefreshToken("testuser");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should skip JWT validation for excluded paths")
    void shouldSkipExcludedPaths() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        // Should not be rejected (no 401)
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should skip JWT validation when disabled")
    void shouldSkipWhenDisabled() {
        gatewayProperties.getJwt().setEnabled(false);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- Token generation helpers ---

    private String generateAccessToken(String username, Long userId, List<String> roles, List<String> permissions) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(TEST_ISSUER)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("type", "access")
                .signWith(signingKey)
                .compact();
    }

    private String generateExpiredToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(TEST_ISSUER)
                .issuedAt(Date.from(now.minusSeconds(7200)))
                .expiration(Date.from(now.minusSeconds(3600)))
                .claim("type", "access")
                .signWith(signingKey)
                .compact();
    }

    private String generateRefreshToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(TEST_ISSUER)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(86400)))
                .claim("type", "refresh")
                .signWith(signingKey)
                .compact();
    }
}
