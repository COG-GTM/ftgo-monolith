package net.chrisrichardson.ftgo.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates signed JWT access tokens and refresh tokens.
 *
 * <p>Tokens are signed using HMAC-SHA256 with the secret configured in
 * {@link JwtProperties}. Each token contains:
 * <ul>
 *   <li>{@code sub} — user ID</li>
 *   <li>{@code roles} — granted roles</li>
 *   <li>{@code permissions} — fine-grained permissions</li>
 *   <li>{@code iss} — issuer</li>
 *   <li>{@code iat} — issued-at timestamp</li>
 *   <li>{@code exp} — expiration timestamp</li>
 * </ul>
 */
public class JwtTokenProvider {

    private final JwtProperties properties;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates a signed access token for the given user.
     *
     * @param userId      the user identifier (becomes the {@code sub} claim)
     * @param roles       the user's roles (e.g., {@code ["ROLE_ADMIN", "ROLE_USER"]})
     * @param permissions the user's fine-grained permissions (e.g., {@code ["order:read", "order:write"]})
     * @return a compact, signed JWT string
     */
    public String createAccessToken(String userId, Collection<String> roles, Collection<String> permissions) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + properties.getExpirationMs());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuer(properties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, properties.getSecret())
                .compact();
    }

    /**
     * Creates a signed refresh token for the given user.
     *
     * <p>Refresh tokens have a longer expiration and contain only the
     * user ID and token type — no roles or permissions.
     *
     * @param userId the user identifier
     * @return a compact, signed JWT refresh token string
     */
    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + properties.getRefreshExpirationMs());

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuer(properties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, properties.getSecret())
                .compact();
    }

    /**
     * Creates an access token with additional custom claims.
     *
     * @param userId       the user identifier
     * @param roles        the user's roles
     * @param permissions  the user's permissions
     * @param customClaims additional claims to include in the token
     * @return a compact, signed JWT string
     */
    public String createAccessToken(String userId, Collection<String> roles,
                                    Collection<String> permissions,
                                    Map<String, Object> customClaims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + properties.getExpirationMs());

        Map<String, Object> claims = new HashMap<>();
        if (customClaims != null) {
            claims.putAll(customClaims);
        }
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuer(properties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, properties.getSecret())
                .compact();
    }
}
