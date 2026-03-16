package net.chrisrichardson.ftgo.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Spring Security {@link org.springframework.security.core.Authentication} implementation
 * that represents a JWT-authenticated principal.
 *
 * <p>Once the {@link JwtAuthenticationFilter} validates a token, it creates an instance
 * of this class and places it in the {@link org.springframework.security.core.context.SecurityContext}.
 * Downstream code can then retrieve the user ID, roles, and permissions without
 * re-parsing the JWT.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final String userId;
    private final List<String> roles;
    private final List<String> permissions;

    /**
     * Creates an authenticated JWT token.
     *
     * @param userId      the user identifier from the {@code sub} claim
     * @param roles       the user's roles
     * @param permissions the user's permissions
     * @param authorities Spring Security authorities derived from roles and permissions
     */
    public JwtAuthenticationToken(String userId,
                                  List<String> roles,
                                  List<String> permissions,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userId = userId;
        this.roles = roles != null ? Collections.unmodifiableList(roles) : Collections.<String>emptyList();
        this.permissions = permissions != null ? Collections.unmodifiableList(permissions) : Collections.<String>emptyList();
        setAuthenticated(true);
    }

    /**
     * Returns {@code null} — JWT authentication is stateless and has no credentials.
     */
    @Override
    public Object getCredentials() {
        return null;
    }

    /**
     * Returns the user ID (the {@code sub} claim).
     */
    @Override
    public Object getPrincipal() {
        return userId;
    }

    /**
     * Returns the user ID extracted from the JWT {@code sub} claim.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the user's roles from the JWT {@code roles} claim.
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Returns the user's permissions from the JWT {@code permissions} claim.
     */
    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public String getName() {
        return userId;
    }
}
