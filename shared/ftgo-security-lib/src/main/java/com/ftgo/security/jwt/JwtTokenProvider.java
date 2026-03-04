package com.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT token provider for creating, parsing, and validating JWT tokens.
 *
 * <p>This component handles all JWT operations:
 * <ul>
 *   <li><strong>Token creation</strong> — generates signed access and refresh tokens</li>
 *   <li><strong>Token parsing</strong> — extracts claims from valid tokens</li>
 *   <li><strong>Token validation</strong> — verifies signature, expiration, and issuer</li>
 *   <li><strong>User extraction</strong> — builds {@link FtgoUserDetails} from claims</li>
 * </ul>
 *
 * <p>Tokens are signed using HMAC-SHA256 (HS256) with a configurable secret key.
 * The secret key must be provided via the {@code ftgo.security.jwt.secret} property
 * (typically from an environment variable).
 *
 * <p>Token structure:
 * <pre>
 * Header:  { "alg": "HS256", "typ": "JWT" }
 * Payload: {
 *   "sub": "user-id-123",
 *   "username": "john.doe",
 *   "roles": ["ROLE_USER", "ROLE_ADMIN"],
 *   "iss": "ftgo-platform",
 *   "iat": 1709568000,
 *   "exp": 1709571600,
 *   "type": "access",
 *   ... (service-specific claims)
 * }
 * </pre>
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TOKEN_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final FtgoJwtProperties jwtProperties;

    public JwtTokenProvider(FtgoJwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * Generates an access token for the given user.
     *
     * @param userId   the user ID (stored in {@code sub} claim)
     * @param username the username
     * @param roles    the user's roles
     * @return the signed JWT access token string
     */
    public String generateAccessToken(String userId, String username, Collection<String> roles) {
        return generateAccessToken(userId, username, roles, Collections.emptyMap());
    }

    /**
     * Generates an access token with additional custom claims.
     *
     * @param userId       the user ID (stored in {@code sub} claim)
     * @param username     the username
     * @param roles        the user's roles
     * @param customClaims additional service-specific claims
     * @return the signed JWT access token string
     */
    public String generateAccessToken(String userId, String username, Collection<String> roles,
                                      Map<String, Object> customClaims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpirationMs());

        Map<String, Object> claims = new HashMap<>();
        if (customClaims != null) {
            claims.putAll(customClaims);
        }
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLES, roles != null ? new ArrayList<>(roles) : Collections.emptyList());
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Generates a refresh token for the given user.
     *
     * <p>Refresh tokens have a longer expiration than access tokens and
     * contain minimal claims (only user ID and token type).
     *
     * @param userId the user ID
     * @return the signed JWT refresh token string
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getRefreshExpirationMs());

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Validates the given JWT token.
     *
     * <p>Checks signature validity, expiration, and issuer.
     *
     * @param token the JWT token string
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaimsFromToken(token);
            // Verify issuer matches
            String issuer = claims.getIssuer();
            if (issuer != null && !issuer.equals(jwtProperties.getIssuer())) {
                log.warn("JWT token has invalid issuer: {}", issuer);
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts {@link FtgoUserDetails} from a valid JWT token.
     *
     * @param token the JWT token string
     * @return the user details extracted from the token
     * @throws JwtException if the token is invalid or expired
     */
    public FtgoUserDetails extractUserDetails(String token) {
        Claims claims = parseClaimsFromToken(token);

        String userId = claims.getSubject();
        String username = claims.get(CLAIM_USERNAME, String.class);
        if (username == null) {
            username = userId;
        }

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(CLAIM_ROLES, List.class);
        if (roles == null) {
            roles = Collections.emptyList();
        }

        // Build full claims map
        Map<String, Object> allClaims = new HashMap<>(claims);

        return new FtgoUserDetails(userId, username, roles, allClaims);
    }

    /**
     * Checks whether the given token is a refresh token.
     *
     * @param token the JWT token string
     * @return {@code true} if the token type is "refresh"
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseClaimsFromToken(token);
            return TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Checks whether the given token is an access token.
     *
     * @param token the JWT token string
     * @return {@code true} if the token type is "access"
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = parseClaimsFromToken(token);
            return TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extracts the user ID (subject) from the given token.
     *
     * @param token the JWT token string
     * @return the user ID
     * @throws JwtException if the token is invalid
     */
    public String extractUserId(String token) {
        return parseClaimsFromToken(token).getSubject();
    }

    /**
     * Extracts the expiration date from the given token.
     *
     * @param token the JWT token string
     * @return the expiration date
     * @throws JwtException if the token is invalid
     */
    public Date extractExpiration(String token) {
        return parseClaimsFromToken(token).getExpiration();
    }

    /**
     * Parses and validates the JWT token, returning the claims.
     *
     * @param token the JWT token string
     * @return the parsed claims
     * @throws JwtException         if the token signature is invalid
     * @throws ExpiredJwtException  if the token has expired
     */
    private Claims parseClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Derives the HMAC-SHA signing key from the configured secret.
     *
     * @return the signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
