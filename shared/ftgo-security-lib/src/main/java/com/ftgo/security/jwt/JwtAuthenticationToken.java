package com.ftgo.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Custom {@link org.springframework.security.core.Authentication} token for JWT-based authentication.
 *
 * <p>This token is set in the {@code SecurityContextHolder} after successful JWT
 * validation. It carries the {@link FtgoUserDetails} as its principal and the
 * raw JWT token string as its credentials.
 *
 * <p>The granted authorities are derived from the {@code roles} claim in the JWT,
 * prefixed with {@code ROLE_} to follow Spring Security conventions.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final FtgoUserDetails userDetails;
    private final String token;

    /**
     * Creates a new authenticated JWT token.
     *
     * @param userDetails the user details extracted from the JWT
     * @param token       the raw JWT token string
     * @param authorities the granted authorities derived from JWT roles
     */
    public JwtAuthenticationToken(FtgoUserDetails userDetails, String token,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userDetails = userDetails;
        this.token = token;
        setAuthenticated(true);
    }

    /**
     * Returns the raw JWT token string.
     *
     * @return the JWT token
     */
    @Override
    public Object getCredentials() {
        return token;
    }

    /**
     * Returns the {@link FtgoUserDetails} extracted from the JWT.
     *
     * @return the user details
     */
    @Override
    public Object getPrincipal() {
        return userDetails;
    }

    /**
     * Returns the {@link FtgoUserDetails} with proper typing.
     *
     * @return the user details
     */
    public FtgoUserDetails getUserDetails() {
        return userDetails;
    }

    /**
     * Returns the username from the JWT (delegates to {@link FtgoUserDetails#getUsername()}).
     *
     * @return the username
     */
    @Override
    public String getName() {
        return userDetails.getUsername();
    }
}
