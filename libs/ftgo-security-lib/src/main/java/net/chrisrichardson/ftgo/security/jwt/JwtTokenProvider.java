package net.chrisrichardson.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT token provider for FTGO microservices.
 * Handles token generation, validation, and claims extraction.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${ftgo.jwt.secret:default-secret-key-for-development-only-change-in-production}") String secret,
            @Value("${ftgo.jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs,
            @Value("${ftgo.jwt.refresh-token-expiration-ms:86400000}") long refreshTokenExpirationMs,
            @Value("${ftgo.jwt.issuer:ftgo-auth}") String issuer) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.issuer = issuer;
    }

    public String generateAccessToken(String subject, List<String> roles) {
        return generateToken(subject, roles, accessTokenExpirationMs, "access");
    }

    public String generateRefreshToken(String subject) {
        return generateToken(subject, null, refreshTokenExpirationMs, "refresh");
    }

    private String generateToken(String subject, List<String> roles, long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiry)
                .claim("type", tokenType);

        if (roles != null) {
            builder.claim("roles", roles);
        }

        return builder.signWith(signingKey).compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return parseToken(token).get("roles", List.class);
    }
}
