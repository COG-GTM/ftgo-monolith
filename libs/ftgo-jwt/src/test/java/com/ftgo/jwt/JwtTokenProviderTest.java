package com.ftgo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "this-is-a-very-secure-secret-key-for-testing-jwt-operations-256bit";
    private static final String ISSUER = "ftgo-test";
    private static final long EXPIRATION = 3600000;

    private JwtTokenProvider tokenProvider;
    private JwtProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);
        properties.setExpiration(EXPIRATION);
        tokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    void shouldGenerateTokenWithSubject() {
        String token = tokenProvider.generateToken("user@example.com");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(tokenProvider.getSubject(token)).isEqualTo("user@example.com");
    }

    @Test
    void shouldGenerateTokenWithCustomClaims() {
        Map<String, Object> claims = Map.of(
                "roles", List.of("ROLE_USER", "ROLE_ADMIN"),
                "tenantId", "tenant-123"
        );

        String token = tokenProvider.generateToken("admin@example.com", claims);

        Claims parsed = tokenProvider.getClaims(token);
        assertThat(parsed.getSubject()).isEqualTo("admin@example.com");
        assertThat(parsed.get("tenantId", String.class)).isEqualTo("tenant-123");
    }

    @Test
    void shouldGenerateTokenWithIssuer() {
        String token = tokenProvider.generateToken("user@example.com");

        Claims claims = tokenProvider.getClaims(token);
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
    }

    @Test
    void shouldGenerateTokenWithIssuedAt() {
        long before = System.currentTimeMillis();
        String token = tokenProvider.generateToken("user@example.com");
        long after = System.currentTimeMillis();

        Claims claims = tokenProvider.getClaims(token);
        long issuedAt = claims.getIssuedAt().getTime();
        assertThat(issuedAt).isBetween(before - 1000, after + 1000);
    }

    @Test
    void shouldGenerateTokenWithExpiration() {
        long before = System.currentTimeMillis();
        String token = tokenProvider.generateToken("user@example.com");
        long after = System.currentTimeMillis();

        Date expiration = tokenProvider.getExpiration(token);
        assertThat(expiration.getTime()).isBetween(before + EXPIRATION - 1000, after + EXPIRATION + 1000);
    }

    @Test
    void shouldValidateValidToken() {
        String token = tokenProvider.generateToken("user@example.com");

        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtProperties shortLivedProps = new JwtProperties();
        shortLivedProps.setSecret(SECRET);
        shortLivedProps.setIssuer(ISSUER);
        shortLivedProps.setExpiration(0);

        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortLivedProps);

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThat(tokenProvider.validateToken("not-a-valid-jwt-token")).isFalse();
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("another-secret-key-that-is-long-enough-for-hmac-sha256-testing");
        otherProps.setIssuer(ISSUER);
        otherProps.setExpiration(EXPIRATION);

        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);
        String token = otherProvider.generateToken("user@example.com");

        assertThat(tokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void shouldRejectNullToken() {
        assertThat(tokenProvider.validateToken(null)).isFalse();
    }

    @Test
    void shouldRejectEmptyToken() {
        assertThat(tokenProvider.validateToken("")).isFalse();
    }

    @Test
    void shouldGetSubjectFromToken() {
        String token = tokenProvider.generateToken("test-user");

        assertThat(tokenProvider.getSubject(token)).isEqualTo("test-user");
    }

    @Test
    void shouldGetRolesFromToken() {
        Map<String, Object> claims = Map.of("roles", List.of("ROLE_USER", "ROLE_ADMIN"));
        String token = tokenProvider.generateToken("user@example.com", claims);

        List<String> roles = tokenProvider.getRoles(token);
        assertThat(roles).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void shouldReturnEmptyRolesWhenNotPresent() {
        String token = tokenProvider.generateToken("user@example.com");

        List<String> roles = tokenProvider.getRoles(token);
        assertThat(roles).isEmpty();
    }

    @Test
    void shouldDetectNonExpiredToken() {
        String token = tokenProvider.generateToken("user@example.com");

        assertThat(tokenProvider.isTokenExpired(token)).isFalse();
    }

    @Test
    void shouldDetectExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        assertThat(tokenProvider.isTokenExpired(expiredToken)).isTrue();
    }

    @Test
    void shouldThrowOnGetClaimsWithExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> tokenProvider.getClaims(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldThrowOnGetClaimsWithInvalidSignature() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("another-secret-key-that-is-long-enough-for-hmac-sha256-testing");
        otherProps.setIssuer(ISSUER);
        otherProps.setExpiration(EXPIRATION);

        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);
        String token = otherProvider.generateToken("user@example.com");

        assertThatThrownBy(() -> tokenProvider.getClaims(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void shouldReturnConfiguredExpiration() {
        assertThat(tokenProvider.getExpirationMillis()).isEqualTo(EXPIRATION);
    }

    @Test
    void shouldReturnConfiguredIssuer() {
        assertThat(tokenProvider.getIssuer()).isEqualTo(ISSUER);
    }
}
