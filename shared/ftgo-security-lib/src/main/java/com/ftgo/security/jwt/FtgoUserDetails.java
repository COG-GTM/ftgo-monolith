package com.ftgo.security.jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Immutable user details extracted from a JWT token.
 *
 * <p>Provides access to the user ID, username, roles, and any
 * service-specific claims carried in the token. This object is
 * stored in the {@link JwtAuthenticationToken} and made available
 * via {@code SecurityContextHolder}.
 *
 * <p>Example usage:
 * <pre>
 * FtgoUserDetails user = SecurityUtils.getCurrentUserDetails().orElseThrow();
 * String userId = user.getUserId();
 * Collection&lt;String&gt; roles = user.getRoles();
 * String tenantId = (String) user.getClaims().get("tenant_id");
 * </pre>
 */
public class FtgoUserDetails {

    private final String userId;
    private final String username;
    private final Collection<String> roles;
    private final Map<String, Object> claims;

    public FtgoUserDetails(String userId, String username, Collection<String> roles,
                           Map<String, Object> claims) {
        this.userId = userId;
        this.username = username;
        this.roles = roles != null ? Collections.unmodifiableCollection(roles) : Collections.emptyList();
        this.claims = claims != null ? Collections.unmodifiableMap(claims) : Collections.emptyMap();
    }

    /**
     * Returns the user ID from the JWT {@code sub} claim.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the username from the JWT {@code username} claim.
     *
     * @return the username, or the user ID if no username claim is present
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the roles assigned to this user from the JWT {@code roles} claim.
     *
     * @return an unmodifiable collection of role strings
     */
    public Collection<String> getRoles() {
        return roles;
    }

    /**
     * Returns all claims from the JWT token.
     *
     * <p>This includes standard claims (sub, iss, exp, etc.) as well as
     * service-specific custom claims.
     *
     * @return an unmodifiable map of all JWT claims
     */
    public Map<String, Object> getClaims() {
        return claims;
    }

    /**
     * Returns a specific claim value by name.
     *
     * @param claimName the claim name
     * @return the claim value, or null if not present
     */
    public Object getClaim(String claimName) {
        return claims.get(claimName);
    }

    @Override
    public String toString() {
        return "FtgoUserDetails{userId='" + userId + "', username='" + username
                + "', roles=" + roles + '}';
    }
}
