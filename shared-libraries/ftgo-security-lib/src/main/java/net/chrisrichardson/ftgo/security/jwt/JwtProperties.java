package net.chrisrichardson.ftgo.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration properties for JWT authentication.
 *
 * <p>All properties are prefixed with {@code ftgo.security.jwt} and can be
 * overridden in each service's {@code application.properties} or
 * {@code application.yml}.
 *
 * <p>Example configuration:
 * <pre>
 * ftgo.security.jwt.secret=myBase64EncodedSecret
 * ftgo.security.jwt.expiration-ms=3600000
 * ftgo.security.jwt.refresh-expiration-ms=86400000
 * ftgo.security.jwt.issuer=ftgo-platform
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.security.jwt")
public class JwtProperties {

    /**
     * Base64-encoded secret key used for HMAC-SHA signing and verification.
     * Must be at least 256 bits (32 bytes) when decoded for HS256.
     */
    private String secret = "";

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
     * Token issuer claim ({@code iss}).
     * Default: {@code ftgo-platform}.
     */
    private String issuer = "ftgo-platform";

    /**
     * HTTP header name used to transmit the JWT.
     * Default: {@code Authorization}.
     */
    private String header = "Authorization";

    /**
     * Prefix prepended to the token in the HTTP header.
     * Default: {@code Bearer }.
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Whether JWT authentication is enabled.
     * Default: {@code true}.
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

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
