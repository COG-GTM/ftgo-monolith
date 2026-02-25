package com.ftgo.security.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable response DTO returned by authentication and token-refresh
 * endpoints.
 * <p>
 * Follows the OAuth 2.0 token response structure (RFC 6749 §5.1) for
 * familiarity, even though the current implementation is not a full
 * OAuth 2.0 server.
 * </p>
 *
 * <pre>
 * {
 *   "access_token": "eyJhbGciOi...",
 *   "refresh_token": "eyJhbGciOi...",
 *   "token_type": "Bearer",
 *   "expires_in": 900
 * }
 * </pre>
 */
public class JwtTokenResponse {

    @JsonProperty("access_token")
    private final String accessToken;

    @JsonProperty("refresh_token")
    private final String refreshToken;

    @JsonProperty("token_type")
    private final String tokenType;

    @JsonProperty("expires_in")
    private final long expiresIn;

    public JwtTokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
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

    public long getExpiresIn() {
        return expiresIn;
    }
}
