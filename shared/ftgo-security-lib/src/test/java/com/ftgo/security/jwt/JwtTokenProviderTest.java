package com.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtTokenProvider}.
 */
class JwtTokenProviderTest {

    // 256-bit test secret (32 bytes) — NEVER use in production
    private static final String TEST_SECRET =
            "ftgo-test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final String TEST_ISSUER = "ftgo-test";

    private JwtTokenProvider tokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setEnabled(true);
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setIssuer(TEST_ISSUER);
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(15));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));

        tokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Nested
    @DisplayName("Access Token Generation")
    class AccessTokenGeneration {

        @Test
        @DisplayName("should generate a valid access token with all claims")
        void shouldGenerateValidAccessToken() {
            String token = tokenProvider.generateAccessToken(
                    42L, "john.doe",
                    List.of("ROLE_USER", "ROLE_ADMIN"),
                    List.of("order:create", "order:read"));

            assertThat(token).isNotBlank();
            assertThat(tokenProvider.validateToken(token)).isTrue();
            assertThat(tokenProvider.getUsername(token)).isEqualTo("john.doe");
            assertThat(tokenProvider.getUserId(token)).isEqualTo(42L);
            assertThat(tokenProvider.isAccessToken(token)).isTrue();
            assertThat(tokenProvider.isRefreshToken(token)).isFalse();
            assertThat(tokenProvider.getRoles(token))
                    .containsExactly("ROLE_USER", "ROLE_ADMIN");
            assertThat(tokenProvider.getPermissions(token))
                    .containsExactly("order:create", "order:read");
        }

        @Test
        @DisplayName("should set correct issuer claim")
        void shouldSetCorrectIssuer() {
            String token = tokenProvider.generateAccessToken(
                    1L, "user", List.of(), List.of());
            Claims claims = tokenProvider.parseToken(token);
            assertThat(claims.getIssuer()).isEqualTo(TEST_ISSUER);
        }

        @Test
        @DisplayName("should handle null permissions gracefully")
        void shouldHandleNullPermissions() {
            String token = tokenProvider.generateAccessToken(
                    1L, "user", List.of("ROLE_USER"), null);

            assertThat(tokenProvider.getPermissions(token)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Refresh Token Generation")
    class RefreshTokenGeneration {

        @Test
        @DisplayName("should generate a valid refresh token")
        void shouldGenerateValidRefreshToken() {
            String token = tokenProvider.generateRefreshToken("john.doe");

            assertThat(token).isNotBlank();
            assertThat(tokenProvider.validateToken(token)).isTrue();
            assertThat(tokenProvider.getUsername(token)).isEqualTo("john.doe");
            assertThat(tokenProvider.isRefreshToken(token)).isTrue();
            assertThat(tokenProvider.isAccessToken(token)).isFalse();
        }

        @Test
        @DisplayName("refresh token should not carry roles or permissions")
        void refreshTokenShouldNotCarryRolesOrPermissions() {
            String token = tokenProvider.generateRefreshToken("user");

            assertThat(tokenProvider.getRoles(token)).isEmpty();
            assertThat(tokenProvider.getPermissions(token)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("should reject expired tokens")
        void shouldRejectExpiredTokens() {
            // Create a provider with very short expiry
            JwtProperties shortExpiry = new JwtProperties();
            shortExpiry.setSecret(TEST_SECRET);
            shortExpiry.setIssuer(TEST_ISSUER);
            shortExpiry.setAccessTokenExpiration(Duration.ofMillis(1));
            JwtTokenProvider shortProvider = new JwtTokenProvider(shortExpiry);

            String token = shortProvider.generateAccessToken(
                    1L, "user", List.of(), List.of());

            // Wait for token to expire
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}

            assertThat(shortProvider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("should reject tokens with wrong signature")
        void shouldRejectTokensWithWrongSignature() {
            // Generate token with a different secret
            JwtProperties otherProps = new JwtProperties();
            otherProps.setSecret("another-secret-key-that-is-at-least-32-bytes-long!!");
            otherProps.setIssuer(TEST_ISSUER);
            otherProps.setAccessTokenExpiration(Duration.ofMinutes(15));
            JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

            String token = otherProvider.generateAccessToken(
                    1L, "user", List.of(), List.of());

            assertThat(tokenProvider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("should reject malformed tokens")
        void shouldRejectMalformedTokens() {
            assertThat(tokenProvider.validateToken("not.a.valid.jwt")).isFalse();
        }

        @Test
        @DisplayName("should reject null and blank tokens")
        void shouldRejectNullAndBlankTokens() {
            assertThat(tokenProvider.validateToken(null)).isFalse();
            assertThat(tokenProvider.validateToken("")).isFalse();
            assertThat(tokenProvider.validateToken("   ")).isFalse();
        }

        @Test
        @DisplayName("should reject tokens with wrong issuer")
        void shouldRejectTokensWithWrongIssuer() {
            // Create a token with a different issuer
            SecretKey key = Keys.hmacShaKeyFor(
                    TEST_SECRET.getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .subject("user")
                    .issuer("wrong-issuer")
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusSeconds(300)))
                    .signWith(key)
                    .compact();

            assertThat(tokenProvider.validateToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("Spring Security Integration")
    class SpringSecurityIntegration {

        @Test
        @DisplayName("should build Authentication from access token")
        void shouldBuildAuthenticationFromAccessToken() {
            String token = tokenProvider.generateAccessToken(
                    42L, "john.doe",
                    List.of("ROLE_USER", "ROLE_ADMIN"),
                    List.of("order:create"));

            Authentication auth = tokenProvider.getAuthentication(token);

            assertThat(auth).isNotNull();
            assertThat(auth.isAuthenticated()).isTrue();
            assertThat(auth.getName()).isEqualTo("john.doe");
            assertThat(auth.getCredentials()).isEqualTo(token);

            JwtUserDetails principal = (JwtUserDetails) auth.getPrincipal();
            assertThat(principal.getUserId()).isEqualTo(42L);
            assertThat(principal.getUsername()).isEqualTo("john.doe");
            assertThat(principal.getRoles()).containsExactly("ROLE_USER", "ROLE_ADMIN");
            assertThat(principal.getPermissions()).containsExactly("order:create");

            assertThat(auth.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER", "ROLE_ADMIN");
        }
    }
}
