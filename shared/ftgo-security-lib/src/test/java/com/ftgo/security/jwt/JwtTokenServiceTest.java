package com.ftgo.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtTokenService}.
 */
class JwtTokenServiceTest {

    private static final String TEST_SECRET =
            "ftgo-test-secret-key-that-is-at-least-32-bytes-long!!";

    private JwtTokenProvider tokenProvider;
    private JwtTokenService tokenService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setEnabled(true);
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setIssuer("ftgo-test");
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(15));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));

        tokenProvider = new JwtTokenProvider(jwtProperties);
        tokenService = new JwtTokenService(tokenProvider, jwtProperties);
    }

    @Test
    @DisplayName("should issue access and refresh token pair")
    void shouldIssueTokenPair() {
        JwtTokenResponse response = tokenService.issueTokens(
                42L, "john.doe",
                List.of("ROLE_USER"), List.of("order:create"));

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900L); // 15 minutes

        // Verify access token claims
        assertThat(tokenProvider.isAccessToken(response.getAccessToken())).isTrue();
        assertThat(tokenProvider.getUsername(response.getAccessToken())).isEqualTo("john.doe");
        assertThat(tokenProvider.getUserId(response.getAccessToken())).isEqualTo(42L);

        // Verify refresh token
        assertThat(tokenProvider.isRefreshToken(response.getRefreshToken())).isTrue();
    }

    @Test
    @DisplayName("should refresh access token using valid refresh token")
    void shouldRefreshAccessToken() throws InterruptedException {
        // Issue initial tokens
        JwtTokenResponse initial = tokenService.issueTokens(
                42L, "john.doe", List.of("ROLE_USER"), List.of());

        // Small delay so that the iat/exp claims differ (token rotation)
        Thread.sleep(1100);

        // Refresh
        JwtTokenResponse refreshed = tokenService.refreshAccessToken(
                initial.getRefreshToken());

        assertThat(refreshed.getAccessToken()).isNotBlank();
        assertThat(refreshed.getRefreshToken()).isNotBlank();
        assertThat(refreshed.getTokenType()).isEqualTo("Bearer");

        // New access token should have the same username
        assertThat(tokenProvider.getUsername(refreshed.getAccessToken()))
                .isEqualTo("john.doe");

        // New tokens should be valid
        assertThat(tokenProvider.validateToken(refreshed.getAccessToken())).isTrue();
        assertThat(tokenProvider.validateToken(refreshed.getRefreshToken())).isTrue();
        assertThat(tokenProvider.isAccessToken(refreshed.getAccessToken())).isTrue();
        assertThat(tokenProvider.isRefreshToken(refreshed.getRefreshToken())).isTrue();
    }

    @Test
    @DisplayName("should reject access token used as refresh token")
    void shouldRejectAccessTokenAsRefreshToken() {
        JwtTokenResponse response = tokenService.issueTokens(
                1L, "user", List.of(), List.of());

        assertThatThrownBy(() -> tokenService.refreshAccessToken(response.getAccessToken()))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("not a refresh token");
    }

    @Test
    @DisplayName("should reject invalid refresh token")
    void shouldRejectInvalidRefreshToken() {
        assertThatThrownBy(() -> tokenService.refreshAccessToken("invalid.token.here"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("invalid or expired");
    }

    @Test
    @DisplayName("should reject expired refresh token")
    void shouldRejectExpiredRefreshToken() {
        // Create provider with very short refresh expiry
        JwtProperties shortExpiry = new JwtProperties();
        shortExpiry.setSecret(TEST_SECRET);
        shortExpiry.setIssuer("ftgo-test");
        shortExpiry.setAccessTokenExpiration(Duration.ofMinutes(15));
        shortExpiry.setRefreshTokenExpiration(Duration.ofMillis(1));

        JwtTokenProvider shortProvider = new JwtTokenProvider(shortExpiry);
        JwtTokenService shortService = new JwtTokenService(shortProvider, shortExpiry);

        String refreshToken = shortProvider.generateRefreshToken("user");

        // Wait for expiry
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}

        assertThatThrownBy(() -> shortService.refreshAccessToken(refreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
