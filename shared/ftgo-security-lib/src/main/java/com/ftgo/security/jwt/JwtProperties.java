package com.ftgo.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for JWT authentication.
 * <p>
 * Bind via {@code ftgo.security.jwt.*} in application.properties or application.yml.
 * </p>
 *
 * <pre>
 * ftgo.security.jwt.enabled=true
 * ftgo.security.jwt.secret=${JWT_SECRET}
 * ftgo.security.jwt.issuer=ftgo-platform
 * ftgo.security.jwt.access-token-expiration=PT15M
 * ftgo.security.jwt.refresh-token-expiration=P7D
 * </pre>
 *
 * <p><strong>Security Note:</strong> The {@code secret} property must NEVER be
 * hardcoded. Always inject via environment variable ({@code JWT_SECRET})
 * or a secrets manager.</p>
 */
@ConfigurationProperties(prefix = "ftgo.security.jwt")
public class JwtProperties {

    /**
     * Whether JWT authentication is enabled. When disabled, the JWT filter
     * is not registered and the security configuration falls back to
     * HTTP Basic (from EM-39).
     */
    private boolean enabled = false;

    /**
     * Base64-encoded HMAC-SHA secret used to sign and verify tokens.
     * Must be at least 256 bits (32 bytes) for HS256.
     * <p>
     * <strong>NEVER hardcode this value.</strong> Use:
     * {@code ftgo.security.jwt.secret=${JWT_SECRET}}
     * </p>
     */
    private String secret;

    /**
     * Token issuer claim ({@code iss}). Used for validation.
     */
    private String issuer = "ftgo-platform";

    /**
     * Access token lifetime. Default: 15 minutes.
     */
    private Duration accessTokenExpiration = Duration.ofMinutes(15);

    /**
     * Refresh token lifetime. Default: 7 days.
     */
    private Duration refreshTokenExpiration = Duration.ofDays(7);

    /**
     * Audiences that this service accepts. If empty, audience validation
     * is skipped.
     */
    private List<String> audiences = new ArrayList<>();

    // --- Getters and Setters ---

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Duration accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public List<String> getAudiences() {
        return audiences;
    }

    public void setAudiences(List<String> audiences) {
        this.audiences = audiences;
    }
}
