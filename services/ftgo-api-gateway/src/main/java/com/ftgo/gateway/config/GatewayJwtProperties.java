package com.ftgo.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties for the API Gateway.
 *
 * <p>Mirrors the JWT properties from ftgo-security-lib but scoped for the
 * reactive gateway context. Properties are prefixed with {@code ftgo.security.jwt}.
 */
@Component
@ConfigurationProperties(prefix = "ftgo.security.jwt")
public class GatewayJwtProperties {

    /**
     * HMAC-SHA secret key for signing and verifying JWT tokens.
     * Must be provided via environment variable (e.g., FTGO_SECURITY_JWT_SECRET).
     */
    private String secret;

    /**
     * Token issuer claim (iss). Default: "ftgo-platform".
     */
    private String issuer = "ftgo-platform";

    /**
     * Whether JWT authentication is enabled. Default: true.
     */
    private boolean enabled = true;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
