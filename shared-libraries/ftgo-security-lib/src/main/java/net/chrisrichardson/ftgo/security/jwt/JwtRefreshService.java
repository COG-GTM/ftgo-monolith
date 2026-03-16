package net.chrisrichardson.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * Service that handles JWT token refresh operations.
 *
 * <p>The refresh flow:
 * <ol>
 *   <li>Client sends the refresh token to the refresh endpoint</li>
 *   <li>This service validates the refresh token</li>
 *   <li>If valid, a new access token (and optionally a new refresh token) is issued</li>
 * </ol>
 *
 * <p>Note: This service provides the token-level refresh logic. The actual
 * refresh endpoint (REST controller) is implemented in each service that
 * requires refresh capability.
 */
public class JwtRefreshService {

    private static final Logger log = LoggerFactory.getLogger(JwtRefreshService.class);

    private final JwtTokenProvider tokenProvider;
    private final JwtTokenValidator tokenValidator;

    public JwtRefreshService(JwtTokenProvider tokenProvider, JwtTokenValidator tokenValidator) {
        this.tokenProvider = tokenProvider;
        this.tokenValidator = tokenValidator;
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * <p>Validates the refresh token, extracts the user ID, and issues a new
     * access token with the provided roles and permissions.
     *
     * @param refreshToken the compact refresh JWT string
     * @param roles        the user's current roles (looked up from the user store)
     * @param permissions  the user's current permissions
     * @return a {@link TokenPair} containing new access and refresh tokens,
     *         or {@code null} if the refresh token is invalid
     */
    public TokenPair refresh(String refreshToken, Collection<String> roles, Collection<String> permissions) {
        Claims claims = tokenValidator.validateAndExtractClaims(refreshToken);

        if (claims == null) {
            log.warn("Token refresh failed: invalid or expired refresh token");
            return null;
        }

        if (!tokenValidator.isRefreshToken(claims)) {
            log.warn("Token refresh failed: provided token is not a refresh token");
            return null;
        }

        String userId = JwtClaimsExtractor.getUserId(claims);
        String newAccessToken = tokenProvider.createAccessToken(userId, roles, permissions);
        String newRefreshToken = tokenProvider.createRefreshToken(userId);

        log.debug("Tokens refreshed for user: {}", userId);
        return new TokenPair(newAccessToken, newRefreshToken);
    }

    /**
     * Refreshes an access token using a valid refresh token with default empty roles/permissions.
     *
     * <p>This overload is useful when roles and permissions are embedded in the
     * refresh token or will be looked up by the calling service.
     *
     * @param refreshToken the compact refresh JWT string
     * @return a {@link TokenPair} containing new access and refresh tokens,
     *         or {@code null} if the refresh token is invalid
     */
    public TokenPair refresh(String refreshToken) {
        return refresh(refreshToken, Collections.<String>emptyList(), Collections.<String>emptyList());
    }

    /**
     * Immutable holder for a pair of access and refresh tokens.
     */
    public static class TokenPair {

        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
