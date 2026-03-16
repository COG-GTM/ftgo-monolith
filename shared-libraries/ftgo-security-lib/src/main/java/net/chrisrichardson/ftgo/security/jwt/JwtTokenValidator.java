package net.chrisrichardson.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates JWT tokens and extracts their claims.
 *
 * <p>Validation includes:
 * <ul>
 *   <li>Signature verification against the configured secret</li>
 *   <li>Expiration check</li>
 *   <li>Structural validity (well-formed JWT)</li>
 * </ul>
 *
 * <p>Each validation failure is logged at WARN level with the specific reason,
 * but the exception details are not exposed to the caller to prevent
 * information leakage.
 */
public class JwtTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);

    private final JwtProperties properties;

    public JwtTokenValidator(JwtProperties properties) {
        this.properties = properties;
    }

    /**
     * Validates the token and returns its claims if valid.
     *
     * @param token the compact JWT string (without the Bearer prefix)
     * @return the parsed {@link Claims}, or {@code null} if the token is invalid
     */
    public Claims validateAndExtractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(properties.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.warn("JWT signature validation failed: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty or null: {}", ex.getMessage());
        }
        return null;
    }

    /**
     * Checks whether the given token is structurally valid and not expired.
     *
     * @param token the compact JWT string
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean isValid(String token) {
        return validateAndExtractClaims(token) != null;
    }

    /**
     * Checks whether the given token is a refresh token.
     *
     * @param claims the parsed claims
     * @return {@code true} if the token type is {@code refresh}
     */
    public boolean isRefreshToken(Claims claims) {
        String type = claims.get("type", String.class);
        return "refresh".equals(type);
    }

    /**
     * Checks whether the given token is an access token.
     *
     * @param claims the parsed claims
     * @return {@code true} if the token type is {@code access}
     */
    public boolean isAccessToken(Claims claims) {
        String type = claims.get("type", String.class);
        return "access".equals(type);
    }
}
