package com.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core JWT token provider responsible for token generation, parsing,
 * and validation.
 * <p>
 * Supports two token types:
 * <ul>
 *   <li><strong>Access token</strong> – short-lived, carries user claims
 *       (userId, roles, permissions). Used for API authentication.</li>
 *   <li><strong>Refresh token</strong> – longer-lived, used only to
 *       obtain a new access token without re-authentication.</li>
 * </ul>
 * </p>
 * <p>
 * Tokens are signed with HMAC-SHA256 using the secret configured in
 * {@link JwtProperties#getSecret()}. The signing key is derived once
 * at construction time.
 * </p>
 *
 * @see JwtProperties
 * @see JwtAuthenticationFilter
 */
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /** Custom claim key for user roles. */
    public static final String CLAIM_ROLES = "roles";

    /** Custom claim key for service-specific permissions. */
    public static final String CLAIM_PERMISSIONS = "permissions";

    /** Custom claim key for the numeric user ID. */
    public static final String CLAIM_USER_ID = "userId";

    /** Custom claim key that distinguishes access vs refresh tokens. */
    public static final String CLAIM_TOKEN_TYPE = "type";

    /** Token type value for access tokens. */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /** Token type value for refresh tokens. */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------
    // Token Generation
    // ---------------------------------------------------------------

    /**
     * Generates an access token for the given user.
     *
     * @param userId      numeric user identifier
     * @param username    the subject (username / email)
     * @param roles       granted roles (e.g. {@code ROLE_ADMIN})
     * @param permissions fine-grained permissions (e.g. {@code order:create})
     * @return signed JWT access token string
     */
    public String generateAccessToken(Long userId, String username,
                                      List<String> roles, List<String> permissions) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(username)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_PERMISSIONS, permissions != null ? permissions : Collections.emptyList())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generates a refresh token for the given user.
     * <p>
     * Refresh tokens contain minimal claims — only the subject and
     * token type. They are NOT suitable for authorization decisions.
     * </p>
     *
     * @param username the subject
     * @return signed JWT refresh token string
     */
    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(username)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .signWith(signingKey)
                .compact();
    }

    // ---------------------------------------------------------------
    // Token Parsing / Validation
    // ---------------------------------------------------------------

    /**
     * Parses and validates a JWT token string, returning the claims.
     *
     * @param token the raw JWT string
     * @return parsed {@link Claims}
     * @throws JwtException if the token is invalid, expired, or tampered with
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates the token and returns {@code true} if it is well-formed,
     * correctly signed, and not expired.
     *
     * @param token the raw JWT string
     * @return {@code true} if valid
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("JWT token expired: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.debug("JWT token is blank or null: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extracts the username (subject) from a valid token.
     *
     * @param token the raw JWT string
     * @return the subject claim
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extracts the numeric user ID from a valid access token.
     *
     * @param token the raw JWT string
     * @return user ID, or {@code null} if the claim is absent
     */
    public Long getUserId(String token) {
        Object value = parseToken(token).get(CLAIM_USER_ID);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    /**
     * Extracts the token type claim ({@code access} or {@code refresh}).
     *
     * @param token the raw JWT string
     * @return token type string
     */
    public String getTokenType(String token) {
        return parseToken(token).get(CLAIM_TOKEN_TYPE, String.class);
    }

    /**
     * Returns {@code true} if the token is an access token.
     */
    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(getTokenType(token));
    }

    /**
     * Returns {@code true} if the token is a refresh token.
     */
    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }

    /**
     * Extracts the roles claim from the token as a list of strings.
     *
     * @param token the raw JWT string
     * @return list of role strings, or empty list if absent
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object roles = parseToken(token).get(CLAIM_ROLES);
        if (roles instanceof List<?>) {
            return ((List<Object>) roles).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Extracts the permissions claim from the token as a list of strings.
     *
     * @param token the raw JWT string
     * @return list of permission strings, or empty list if absent
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissions(String token) {
        Object perms = parseToken(token).get(CLAIM_PERMISSIONS);
        if (perms instanceof List<?>) {
            return ((List<Object>) perms).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // ---------------------------------------------------------------
    // Spring Security Integration
    // ---------------------------------------------------------------

    /**
     * Builds a Spring Security {@link Authentication} from a valid
     * access token. The principal is a {@link JwtUserDetails} instance
     * that carries userId, roles, and permissions.
     *
     * @param token the raw JWT access token string
     * @return a fully populated authentication token
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseToken(token);

        String username = claims.getSubject();
        Long userId = claims.get(CLAIM_USER_ID) instanceof Number n ? n.longValue() : null;

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(CLAIM_ROLES) instanceof List<?> list
                ? ((List<Object>) list).stream().map(Object::toString).collect(Collectors.toList())
                : Collections.emptyList();

        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get(CLAIM_PERMISSIONS) instanceof List<?> list
                ? ((List<Object>) list).stream().map(Object::toString).collect(Collectors.toList())
                : Collections.emptyList();

        Collection<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        JwtUserDetails userDetails = new JwtUserDetails(userId, username, roles, permissions);

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }
}
