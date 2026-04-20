package net.chrisrichardson.ftgo.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey signingKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;
    private final String issuer;

    public JwtTokenProvider(String secret, long accessTokenValidityMs, long refreshTokenValidityMs, String issuer) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
        this.issuer = issuer;
    }

    public String generateAccessToken(String subject, List<String> roles, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenValidityMs);

        var builder = Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("roles", roles)
                .claim("type", "access");

        additionalClaims.forEach(builder::claim);

        return builder.signWith(signingKey).compact();
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenValidityMs);

        return Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("type", "refresh")
                .signWith(signingKey)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getSubject(String token) {
        return validateToken(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return validateToken(token).get("roles", List.class);
    }

    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
