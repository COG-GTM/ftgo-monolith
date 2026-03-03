package net.chrisrichardson.ftgo.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for FTGO JWT authentication.
 *
 * <p>These properties can be configured in application.yml or application.properties
 * using the prefix {@code ftgo.jwt}.</p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * ftgo:
 *   jwt:
 *     issuer: https://keycloak.ftgo.com/realms/ftgo
 *     secret: ${JWT_SECRET}           # For HMAC signing (dev/test only)
 *     public-key-location: classpath:jwt/public-key.pem  # For RSA/EC verification
 *     expiration-seconds: 3600
 *     refresh-expiration-seconds: 86400
 *     token-prefix: "Bearer "
 *     header-name: Authorization
 *     excluded-paths:
 *       - /actuator/health
 *       - /actuator/info
 * </pre>
 *
 * <p><strong>Security Note:</strong> Never hardcode signing keys. Always use
 * environment variables or external secret management (e.g., Kubernetes Secrets,
 * HashiCorp Vault) in production.</p>
 */
@ConfigurationProperties(prefix = "ftgo.jwt")
public class FtgoJwtProperties {

    /**
     * JWT token issuer URI (e.g., Keycloak realm URL).
     * Used to validate the 'iss' claim in tokens.
     */
    private String issuer = "https://keycloak.ftgo.com/realms/ftgo";

    /**
     * HMAC secret key for token signing/verification.
     * Must be at least 256 bits (32 bytes) for HS256.
     * Should be provided via environment variable: ${JWT_SECRET}
     *
     * <p><strong>Warning:</strong> Only use HMAC for development/testing.
     * Production should use RSA or EC keys via {@code publicKeyLocation}.</p>
     */
    private String secret;

    /**
     * Location of the public key for RSA/EC token verification.
     * Supports classpath: and file: prefixes.
     * Example: classpath:jwt/public-key.pem
     */
    private String publicKeyLocation;

    /**
     * JWKS (JSON Web Key Set) URI for dynamic key resolution.
     * Typically the Keycloak certs endpoint.
     * Example: https://keycloak.ftgo.com/realms/ftgo/protocol/openid-connect/certs
     */
    private String jwksUri;

    /**
     * Token expiration time in seconds. Defaults to 3600 (1 hour).
     */
    private long expirationSeconds = 3600L;

    /**
     * Refresh token expiration time in seconds. Defaults to 86400 (24 hours).
     */
    private long refreshExpirationSeconds = 86400L;

    /**
     * Token prefix in the Authorization header. Defaults to "Bearer ".
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Header name for the JWT token. Defaults to "Authorization".
     */
    private String headerName = "Authorization";

    /**
     * Whether JWT authentication is enabled. Defaults to true.
     */
    private boolean enabled = true;

    /**
     * Name of the claim containing user roles. Defaults to "roles".
     */
    private String rolesClaim = "roles";

    /**
     * Name of the claim containing user permissions. Defaults to "permissions".
     */
    private String permissionsClaim = "permissions";

    /**
     * Name of the claim containing the user ID. Defaults to "sub" (subject).
     */
    private String userIdClaim = "sub";

    /**
     * URL paths excluded from JWT validation.
     */
    private List<String> excludedPaths = new ArrayList<>(Arrays.asList(
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info"
    ));

    /**
     * Clock skew allowance in seconds for token expiration validation.
     * Defaults to 30 seconds.
     */
    private long clockSkewSeconds = 30L;

    // --- Getters and Setters ---

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getPublicKeyLocation() {
        return publicKeyLocation;
    }

    public void setPublicKeyLocation(String publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }

    public void setRefreshExpirationSeconds(long refreshExpirationSeconds) {
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRolesClaim() {
        return rolesClaim;
    }

    public void setRolesClaim(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }

    public String getPermissionsClaim() {
        return permissionsClaim;
    }

    public void setPermissionsClaim(String permissionsClaim) {
        this.permissionsClaim = permissionsClaim;
    }

    public String getUserIdClaim() {
        return userIdClaim;
    }

    public void setUserIdClaim(String userIdClaim) {
        this.userIdClaim = userIdClaim;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

    /**
     * Returns the excluded paths as a String array for use with antMatchers.
     *
     * @return array of excluded path patterns
     */
    public String[] getExcludedPathsArray() {
        return excludedPaths.toArray(new String[0]);
    }

    public long getClockSkewSeconds() {
        return clockSkewSeconds;
    }

    public void setClockSkewSeconds(long clockSkewSeconds) {
        this.clockSkewSeconds = clockSkewSeconds;
    }
}
