package net.chrisrichardson.ftgo.jwt.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a pair of JWT tokens: an access token and a refresh token.
 *
 * <p>The access token is short-lived and used for API authentication.
 * The refresh token is longer-lived and used to obtain new access tokens
 * without re-authentication.</p>
 *
 * <p>Example response:</p>
 * <pre>
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
 *   "tokenType": "Bearer",
 *   "expiresAt": "2024-01-01T01:00:00Z",
 *   "refreshExpiresAt": "2024-01-02T00:00:00Z"
 * }
 * </pre>
 */
public class TokenPair implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final Instant expiresAt;
    private final Instant refreshExpiresAt;

    public TokenPair(String accessToken, String refreshToken,
                     String tokenType, Instant expiresAt, Instant refreshExpiresAt) {
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken must not be null");
        this.refreshToken = Objects.requireNonNull(refreshToken, "refreshToken must not be null");
        this.tokenType = Objects.requireNonNull(tokenType, "tokenType must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.refreshExpiresAt = Objects.requireNonNull(refreshExpiresAt, "refreshExpiresAt must not be null");
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    /**
     * Checks if the access token has expired.
     *
     * @return true if the access token is expired
     */
    public boolean isAccessTokenExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks if the refresh token has expired.
     *
     * @return true if the refresh token is expired
     */
    public boolean isRefreshTokenExpired() {
        return Instant.now().isAfter(refreshExpiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenPair tokenPair = (TokenPair) o;
        return Objects.equals(accessToken, tokenPair.accessToken)
                && Objects.equals(refreshToken, tokenPair.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken);
    }

    @Override
    public String toString() {
        return "TokenPair{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresAt=" + expiresAt +
                ", refreshExpiresAt=" + refreshExpiresAt +
                '}';
    }
}
