package net.chrisrichardson.ftgo.security.jwt;

import io.jsonwebtoken.Claims;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Extracts typed claim values from a parsed JWT {@link Claims} object.
 *
 * <p>Provides convenient accessor methods for the standard FTGO claims:
 * <ul>
 *   <li>{@code sub} — user ID</li>
 *   <li>{@code roles} — list of role strings</li>
 *   <li>{@code permissions} — list of permission strings</li>
 *   <li>{@code type} — token type ({@code access} or {@code refresh})</li>
 * </ul>
 */
public class JwtClaimsExtractor {

    private JwtClaimsExtractor() {
        // utility class
    }

    /**
     * Extracts the user ID from the {@code sub} claim.
     *
     * @param claims the parsed JWT claims
     * @return the user ID, or {@code null} if not present
     */
    public static String getUserId(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extracts the roles from the {@code roles} claim.
     *
     * @param claims the parsed JWT claims
     * @return an unmodifiable list of role strings, or an empty list if not present
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List) {
            return Collections.unmodifiableList((List<String>) roles);
        }
        return Collections.emptyList();
    }

    /**
     * Extracts the permissions from the {@code permissions} claim.
     *
     * @param claims the parsed JWT claims
     * @return an unmodifiable list of permission strings, or an empty list if not present
     */
    @SuppressWarnings("unchecked")
    public static List<String> getPermissions(Claims claims) {
        Object permissions = claims.get("permissions");
        if (permissions instanceof List) {
            return Collections.unmodifiableList((List<String>) permissions);
        }
        return Collections.emptyList();
    }

    /**
     * Extracts the token type ({@code access} or {@code refresh}).
     *
     * @param claims the parsed JWT claims
     * @return the token type string, or {@code null} if not present
     */
    public static String getTokenType(Claims claims) {
        return claims.get("type", String.class);
    }

    /**
     * Extracts the issuer from the {@code iss} claim.
     *
     * @param claims the parsed JWT claims
     * @return the issuer string, or {@code null} if not present
     */
    public static String getIssuer(Claims claims) {
        return claims.getIssuer();
    }

    /**
     * Extracts the expiration date from the {@code exp} claim.
     *
     * @param claims the parsed JWT claims
     * @return the expiration date, or {@code null} if not present
     */
    public static Date getExpiration(Claims claims) {
        return claims.getExpiration();
    }

    /**
     * Extracts the issued-at date from the {@code iat} claim.
     *
     * @param claims the parsed JWT claims
     * @return the issued-at date, or {@code null} if not present
     */
    public static Date getIssuedAt(Claims claims) {
        return claims.getIssuedAt();
    }
}
