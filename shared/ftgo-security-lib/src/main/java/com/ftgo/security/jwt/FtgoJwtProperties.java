package com.ftgo.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized JWT configuration properties for FTGO microservices.
 *
 * <p>Properties are prefixed with {@code ftgo.security.jwt} and can be set via
 * application.properties, application.yml, or environment variables.
 *
 * <p><strong>Important:</strong> The {@code secret} property should be provided
 * via environment variables (e.g., {@code FTGO_SECURITY_JWT_SECRET}) and never
 * hardcoded in configuration files.
 *
 * <p>Example configuration:
 * <pre>
 * ftgo.security.jwt.secret=${JWT_SECRET}
 * ftgo.security.jwt.expiration-ms=3600000
 * ftgo.security.jwt.refresh-expiration-ms=86400000
 * ftgo.security.jwt.issuer=ftgo-platform
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "ftgo.security.jwt")
public class FtgoJwtProperties {

    /**
     * Base64-encoded HMAC-SHA secret key for signing and verifying JWT tokens.
     * Must be provided via environment variable (e.g., FTGO_SECURITY_JWT_SECRET).
     * Minimum 256 bits (32 bytes) for HS256.
     */
    private String secret;

    /**
     * Access token expiration time in milliseconds.
     * Default: 3600000 (1 hour).
     */
    private long expirationMs = 3600000L;

    /**
     * Refresh token expiration time in milliseconds.
     * Default: 86400000 (24 hours).
     */
    private long refreshExpirationMs = 86400000L;

    /**
     * Token issuer claim (iss).
     * Default: "ftgo-platform".
     */
    private String issuer = "ftgo-platform";

    /**
     * Whether JWT authentication is enabled.
     * Default: true. Set to false to disable JWT validation.
     */
    private boolean enabled = true;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
