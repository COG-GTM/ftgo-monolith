package net.chrisrichardson.ftgo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import net.chrisrichardson.ftgo.security.test.JwtTestSupport;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private final JwtTestSupport jwt = JwtTestSupport.withDefaults();
    private final JwtTokenProvider provider = jwt.getTokenProvider();

    @Test
    void createAccessToken_containsExpectedClaims() {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        List<String> permissions = Arrays.asList("order:read", "order:write");

        String token = provider.createAccessToken("user-123", roles, permissions);
        assertNotNull(token);

        Claims claims = parseToken(token);
        assertEquals("user-123", claims.getSubject());
        assertEquals(JwtTestSupport.TEST_ISSUER, claims.getIssuer());
        assertEquals("access", claims.get("type"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));

        @SuppressWarnings("unchecked")
        List<String> tokenRoles = (List<String>) claims.get("roles");
        assertEquals(roles, tokenRoles);

        @SuppressWarnings("unchecked")
        List<String> tokenPermissions = (List<String>) claims.get("permissions");
        assertEquals(permissions, tokenPermissions);
    }

    @Test
    void createRefreshToken_containsOnlySubjectAndType() {
        String token = provider.createRefreshToken("user-456");
        assertNotNull(token);

        Claims claims = parseToken(token);
        assertEquals("user-456", claims.getSubject());
        assertEquals("refresh", claims.get("type"));
        assertEquals(JwtTestSupport.TEST_ISSUER, claims.getIssuer());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void createAccessToken_withCustomClaims() {
        Map<String, Object> custom = new HashMap<>();
        custom.put("tenantId", "tenant-99");

        String token = provider.createAccessToken("user-789",
                Collections.singletonList("ROLE_USER"),
                Collections.<String>emptyList(),
                custom);
        assertNotNull(token);

        Claims claims = parseToken(token);
        assertEquals("user-789", claims.getSubject());
        assertEquals("tenant-99", claims.get("tenantId"));
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(JwtTestSupport.TEST_SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}
