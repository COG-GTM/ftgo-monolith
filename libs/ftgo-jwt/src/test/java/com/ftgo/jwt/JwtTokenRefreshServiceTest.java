package com.ftgo.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenRefreshServiceTest {

    private static final String SECRET = "this-is-a-very-secure-secret-key-for-testing-jwt-operations-256bit";
    private static final String ISSUER = "ftgo-test";
    private static final long EXPIRATION = 3600000;
    private static final long REFRESH_THRESHOLD = 300000;

    private JwtTokenProvider tokenProvider;
    private JwtTokenRefreshService refreshService;
    private JwtProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);
        properties.setExpiration(EXPIRATION);
        properties.setRefreshThreshold(REFRESH_THRESHOLD);
        tokenProvider = new JwtTokenProvider(properties);
        refreshService = new JwtTokenRefreshService(tokenProvider, properties);
    }

    @Test
    void shouldNotRefreshTokenFarFromExpiry() {
        String token = tokenProvider.generateToken("user@example.com");

        Optional<String> refreshed = refreshService.refreshToken(token);

        assertThat(refreshed).isEmpty();
    }

    @Test
    void shouldRefreshTokenNearExpiry() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String nearExpiryToken = Jwts.builder()
                .subject("user@example.com")
                .issuer(ISSUER)
                .issuedAt(new Date(System.currentTimeMillis() - EXPIRATION + 60000))
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .claim("roles", List.of("ROLE_USER"))
                .signWith(key)
                .compact();

        Optional<String> refreshed = refreshService.refreshToken(nearExpiryToken);

        assertThat(refreshed).isPresent();
        String newToken = refreshed.get();
        assertThat(tokenProvider.validateToken(newToken)).isTrue();
        assertThat(tokenProvider.getSubject(newToken)).isEqualTo("user@example.com");
    }

    @Test
    void shouldPreserveCustomClaimsOnRefresh() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String nearExpiryToken = Jwts.builder()
                .subject("user@example.com")
                .issuer(ISSUER)
                .issuedAt(new Date(System.currentTimeMillis() - EXPIRATION + 60000))
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .claim("roles", List.of("ROLE_ADMIN"))
                .claim("tenantId", "tenant-123")
                .signWith(key)
                .compact();

        Optional<String> refreshed = refreshService.refreshToken(nearExpiryToken);

        assertThat(refreshed).isPresent();
        var claims = tokenProvider.getClaims(refreshed.get());
        assertThat(claims.get("tenantId", String.class)).isEqualTo("tenant-123");
    }

    @Test
    void shouldNotRefreshExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("user@example.com")
                .issuer(ISSUER)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        Optional<String> refreshed = refreshService.refreshToken(expiredToken);

        assertThat(refreshed).isEmpty();
    }

    @Test
    void shouldDetectRefreshableToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String nearExpiryToken = Jwts.builder()
                .subject("user@example.com")
                .issuer(ISSUER)
                .issuedAt(new Date(System.currentTimeMillis() - EXPIRATION + 60000))
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        assertThat(refreshService.isRefreshable(nearExpiryToken)).isTrue();
    }

    @Test
    void shouldDetectNonRefreshableToken() {
        String token = tokenProvider.generateToken("user@example.com");

        assertThat(refreshService.isRefreshable(token)).isFalse();
    }

    @Test
    void shouldDetectNonRefreshableExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        assertThat(refreshService.isRefreshable(expiredToken)).isFalse();
    }

    @Test
    void shouldRefreshWhenWithinThreshold() {
        Date expiration = new Date(System.currentTimeMillis() + 60000);

        assertThat(refreshService.shouldRefresh(expiration)).isTrue();
    }

    @Test
    void shouldNotRefreshWhenFarFromExpiry() {
        Date expiration = new Date(System.currentTimeMillis() + EXPIRATION);

        assertThat(refreshService.shouldRefresh(expiration)).isFalse();
    }

    @Test
    void shouldNotRefreshWhenAlreadyExpired() {
        Date expiration = new Date(System.currentTimeMillis() - 1000);

        assertThat(refreshService.shouldRefresh(expiration)).isFalse();
    }
}
