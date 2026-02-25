package com.ftgo.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * High-level service for JWT token lifecycle management.
 * <p>
 * Provides methods for:
 * <ul>
 *   <li>Issuing an access + refresh token pair</li>
 *   <li>Refreshing an access token using a valid refresh token</li>
 * </ul>
 * </p>
 * <p>
 * This service delegates cryptographic operations to
 * {@link JwtTokenProvider} and adds business-level validation
 * (e.g. ensuring only refresh tokens are accepted for the
 * refresh flow).
 * </p>
 *
 * @see JwtTokenProvider
 * @see JwtTokenResponse
 */
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtTokenProvider tokenProvider, JwtProperties jwtProperties) {
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Issues a new access + refresh token pair for the given user.
     *
     * @param userId      numeric user identifier
     * @param username    the subject (username / email)
     * @param roles       granted roles
     * @param permissions service-specific permissions
     * @return token response containing both tokens and expiry metadata
     */
    public JwtTokenResponse issueTokens(Long userId, String username,
                                        List<String> roles, List<String> permissions) {
        String accessToken = tokenProvider.generateAccessToken(
                userId, username, roles, permissions);
        String refreshToken = tokenProvider.generateRefreshToken(username);

        long expiresInSeconds = jwtProperties.getAccessTokenExpiration().toSeconds();

        log.info("Issued token pair for user '{}'", username);
        return new JwtTokenResponse(accessToken, refreshToken, expiresInSeconds);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * <p>
     * The refresh token is validated for:
     * <ul>
     *   <li>Signature and expiry</li>
     *   <li>Token type must be {@code refresh}</li>
     * </ul>
     * On success, a new access + refresh token pair is issued. The old
     * refresh token is effectively replaced (token rotation).
     * </p>
     *
     * @param refreshToken the current refresh token
     * @return new token response
     * @throws InvalidRefreshTokenException if the token is invalid or not a refresh token
     */
    public JwtTokenResponse refreshAccessToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException("Refresh token is invalid or expired");
        }

        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException("Provided token is not a refresh token");
        }

        String username = tokenProvider.getUsername(refreshToken);

        // Issue a new pair with empty roles/permissions — the caller
        // (typically a gateway or auth service) should enrich these by
        // looking up current user roles from a user store.
        // For a self-contained flow, roles can be re-embedded if the
        // refresh token carried them.
        String newAccessToken = tokenProvider.generateAccessToken(
                null, username, Collections.emptyList(), Collections.emptyList());
        String newRefreshToken = tokenProvider.generateRefreshToken(username);

        long expiresInSeconds = jwtProperties.getAccessTokenExpiration().toSeconds();

        log.info("Refreshed token pair for user '{}'", username);
        return new JwtTokenResponse(newAccessToken, newRefreshToken, expiresInSeconds);
    }
}
