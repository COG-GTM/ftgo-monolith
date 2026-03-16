package net.chrisrichardson.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.chrisrichardson.ftgo.security.test.JwtTestSupport;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenValidatorTest {

    private final JwtTestSupport jwt = JwtTestSupport.withDefaults();
    private final JwtTokenValidator validator = jwt.getTokenValidator();
    private final JwtTokenProvider provider = jwt.getTokenProvider();

    @Test
    void validateAndExtractClaims_validToken_returnsClaims() {
        String token = provider.createAccessToken("user-1",
                Arrays.asList("ROLE_USER"), Collections.<String>emptyList());

        Claims claims = validator.validateAndExtractClaims(token);
        assertNotNull(claims);
        assertEquals("user-1", claims.getSubject());
    }

    @Test
    void validateAndExtractClaims_expiredToken_returnsNull() {
        String token = Jwts.builder()
                .setSubject("user-expired")
                .setIssuedAt(new Date(System.currentTimeMillis() - 20000))
                .setExpiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(SignatureAlgorithm.HS256, JwtTestSupport.TEST_SECRET)
                .compact();

        Claims claims = validator.validateAndExtractClaims(token);
        assertNull(claims);
    }

    @Test
    void validateAndExtractClaims_invalidSignature_returnsNull() {
        String token = Jwts.builder()
                .setSubject("user-bad-sig")
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(SignatureAlgorithm.HS256, "wrong-secret-key-that-is-different")
                .compact();

        Claims claims = validator.validateAndExtractClaims(token);
        assertNull(claims);
    }

    @Test
    void validateAndExtractClaims_malformedToken_returnsNull() {
        Claims claims = validator.validateAndExtractClaims("not.a.jwt");
        assertNull(claims);
    }

    @Test
    void isValid_validToken_returnsTrue() {
        String token = provider.createAccessToken("user-2",
                Collections.singletonList("ROLE_USER"), Collections.<String>emptyList());
        assertTrue(validator.isValid(token));
    }

    @Test
    void isValid_invalidToken_returnsFalse() {
        assertFalse(validator.isValid("garbage-token"));
    }

    @Test
    void isRefreshToken_refreshToken_returnsTrue() {
        String token = provider.createRefreshToken("user-3");
        Claims claims = validator.validateAndExtractClaims(token);
        assertNotNull(claims);
        assertTrue(validator.isRefreshToken(claims));
        assertFalse(validator.isAccessToken(claims));
    }

    @Test
    void isAccessToken_accessToken_returnsTrue() {
        String token = provider.createAccessToken("user-4",
                Collections.singletonList("ROLE_USER"), Collections.<String>emptyList());
        Claims claims = validator.validateAndExtractClaims(token);
        assertNotNull(claims);
        assertTrue(validator.isAccessToken(claims));
        assertFalse(validator.isRefreshToken(claims));
    }
}
