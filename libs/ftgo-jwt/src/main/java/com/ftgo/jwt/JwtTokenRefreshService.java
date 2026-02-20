package com.ftgo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JwtTokenRefreshService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenRefreshService.class);

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties properties;

    public JwtTokenRefreshService(JwtTokenProvider tokenProvider, JwtProperties properties) {
        this.tokenProvider = tokenProvider;
        this.properties = properties;
    }

    public Optional<String> refreshToken(String token) {
        try {
            Claims claims = tokenProvider.getClaims(token);
            if (shouldRefresh(claims.getExpiration())) {
                String subject = claims.getSubject();
                Map<String, Object> customClaims = extractCustomClaims(claims);
                String newToken = tokenProvider.generateToken(subject, customClaims);
                log.debug("Token refreshed for subject: {}", subject);
                return Optional.of(newToken);
            }
            return Optional.empty();
        } catch (ExpiredJwtException ex) {
            log.warn("Cannot refresh expired token: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public boolean shouldRefresh(Date expiration) {
        long timeUntilExpiry = expiration.getTime() - System.currentTimeMillis();
        return timeUntilExpiry > 0 && timeUntilExpiry <= properties.getRefreshThreshold();
    }

    public boolean isRefreshable(String token) {
        try {
            Claims claims = tokenProvider.getClaims(token);
            return shouldRefresh(claims.getExpiration());
        } catch (ExpiredJwtException ex) {
            return false;
        }
    }

    private Map<String, Object> extractCustomClaims(Claims claims) {
        Map<String, Object> customClaims = new HashMap<>();
        claims.forEach((key, value) -> {
            if (!isRegisteredClaim(key)) {
                customClaims.put(key, value);
            }
        });
        return customClaims;
    }

    private boolean isRegisteredClaim(String claimName) {
        return "iss".equals(claimName)
                || "sub".equals(claimName)
                || "exp".equals(claimName)
                || "iat".equals(claimName)
                || "nbf".equals(claimName)
                || "aud".equals(claimName)
                || "jti".equals(claimName);
    }
}
