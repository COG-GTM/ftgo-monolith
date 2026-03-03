package net.chrisrichardson.ftgo.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import net.chrisrichardson.ftgo.jwt.FtgoJwtProperties;
import net.chrisrichardson.ftgo.jwt.model.FtgoUserContext;
import net.chrisrichardson.ftgo.jwt.model.TokenPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Core service for JWT token operations: encoding, decoding, validation, and refresh.
 *
 * <p>This service handles all JWT token lifecycle operations for FTGO microservices:</p>
 * <ul>
 *   <li><strong>Token generation</strong> - Creates signed JWT access and refresh tokens</li>
 *   <li><strong>Token validation</strong> - Validates signature, expiration, and issuer</li>
 *   <li><strong>Claims extraction</strong> - Extracts userId, roles, and permissions from tokens</li>
 *   <li><strong>Token refresh</strong> - Generates new access tokens from valid refresh tokens</li>
 * </ul>
 *
 * <p>The service uses HMAC-SHA256 signing by default, with the secret key configured
 * via {@link FtgoJwtProperties#getSecret()}. The key must be provided via environment
 * variables or external secret management - never hardcoded.</p>
 *
 * @see FtgoJwtProperties
 * @see FtgoUserContext
 * @see TokenPair
 */
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final FtgoJwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtTokenService(FtgoJwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = resolveSigningKey();
    }

    /**
     * Generates a token pair (access token + refresh token) for the given user context.
     *
     * @param userContext the authenticated user context
     * @return a token pair containing access and refresh tokens
     */
    public TokenPair generateTokenPair(FtgoUserContext userContext) {
        Instant now = Instant.now();
        Instant accessExpiry = now.plusSeconds(jwtProperties.getExpirationSeconds());
        Instant refreshExpiry = now.plusSeconds(jwtProperties.getRefreshExpirationSeconds());

        String accessToken = buildToken(userContext, now, accessExpiry, TOKEN_TYPE_ACCESS);
        String refreshToken = buildToken(userContext, now, refreshExpiry, TOKEN_TYPE_REFRESH);

        log.debug("Generated token pair for user: {}", userContext.getUserId());

        return new TokenPair(accessToken, refreshToken, "Bearer", accessExpiry, refreshExpiry);
    }

    /**
     * Generates a single access token for the given user context.
     *
     * @param userContext the authenticated user context
     * @return the signed JWT access token string
     */
    public String generateAccessToken(FtgoUserContext userContext) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.getExpirationSeconds());
        return buildToken(userContext, now, expiry, TOKEN_TYPE_ACCESS);
    }

    /**
     * Validates a JWT token and extracts the claims.
     *
     * @param token the JWT token string (without Bearer prefix)
     * @return the validated claims
     * @throws ExpiredJwtException if the token has expired
     * @throws MalformedJwtException if the token format is invalid
     * @throws SignatureException if the token signature is invalid
     * @throws JwtException for other JWT validation errors
     */
    public Claims validateAndExtractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .setAllowedClockSkewSeconds(jwtProperties.getClockSkewSeconds())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts a {@link FtgoUserContext} from validated JWT claims.
     *
     * @param claims the JWT claims
     * @return the user context populated from the claims
     */
    public FtgoUserContext extractUserContext(Claims claims) {
        String userId = claims.get(jwtProperties.getUserIdClaim(), String.class);
        if (userId == null) {
            userId = claims.getSubject();
        }

        String username = claims.get(CLAIM_USERNAME, String.class);

        List<String> roles = extractListClaim(claims, jwtProperties.getRolesClaim());
        List<String> permissions = extractListClaim(claims, jwtProperties.getPermissionsClaim());

        return FtgoUserContext.builder()
                .userId(userId)
                .username(username != null ? username : userId)
                .roles(roles)
                .permissions(permissions)
                .tokenId(claims.getId())
                .issuer(claims.getIssuer())
                .build();
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * <p>Validates the refresh token, verifies it is a refresh-type token,
     * then generates a new access token with the same user claims.</p>
     *
     * @param refreshToken the refresh token string
     * @return a new token pair with a fresh access token and the same refresh token
     * @throws JwtException if the refresh token is invalid or expired
     * @throws IllegalArgumentException if the token is not a refresh token
     */
    public TokenPair refreshAccessToken(String refreshToken) {
        Claims claims = validateAndExtractClaims(refreshToken);

        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
            throw new IllegalArgumentException("Token is not a refresh token");
        }

        FtgoUserContext userContext = extractUserContext(claims);
        Instant now = Instant.now();
        Instant accessExpiry = now.plusSeconds(jwtProperties.getExpirationSeconds());

        String newAccessToken = buildToken(userContext, now, accessExpiry, TOKEN_TYPE_ACCESS);

        // Keep the original refresh token expiry
        Instant refreshExpiry = claims.getExpiration().toInstant();

        log.debug("Refreshed access token for user: {}", userContext.getUserId());

        return new TokenPair(newAccessToken, refreshToken, "Bearer", accessExpiry, refreshExpiry);
    }

    /**
     * Validates a token and returns whether it is valid.
     *
     * @param token the JWT token string
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }
            validateAndExtractClaims(token);
            return true;
        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a token is a refresh token.
     *
     * @param token the JWT token string
     * @return true if the token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = validateAndExtractClaims(token);
            return TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
        } catch (JwtException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String buildToken(FtgoUserContext userContext, Instant issuedAt,
                              Instant expiration, String tokenType) {
        return Jwts.builder()
                .setSubject(userContext.getUserId())
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .setId(UUID.randomUUID().toString())
                .claim(CLAIM_USERNAME, userContext.getUsername())
                .claim(CLAIM_ROLES, new ArrayList<>(userContext.getRoles()))
                .claim(CLAIM_PERMISSIONS, new ArrayList<>(userContext.getPermissions()))
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractListClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof List) {
            return (List<String>) value;
        }
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            for (String item : strValue.split(",")) {
                result.add(item.trim());
            }
            return result;
        }
        return Collections.emptyList();
    }

    private SecretKey resolveSigningKey() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isEmpty()) {
            log.warn("JWT secret is not configured. Generating a random key for development. "
                    + "This is NOT suitable for production or multi-instance deployments.");
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        // Ensure the key is at least 256 bits for HS256
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 256 bits (32 bytes) for HS256. "
                            + "Current key is " + keyBytes.length + " bytes. "
                            + "Set ftgo.jwt.secret via environment variable JWT_SECRET.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
