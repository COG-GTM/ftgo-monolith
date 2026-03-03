package net.chrisrichardson.ftgo.jwt;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link FtgoJwtProperties}.
 *
 * <p>Tests verify default values, property getters/setters, and
 * configuration behavior.</p>
 */
public class FtgoJwtPropertiesTest {

    @Test
    public void shouldHaveCorrectDefaults() {
        FtgoJwtProperties properties = new FtgoJwtProperties();

        assertEquals("https://keycloak.ftgo.com/realms/ftgo", properties.getIssuer());
        assertNull(properties.getSecret());
        assertNull(properties.getPublicKeyLocation());
        assertNull(properties.getJwksUri());
        assertEquals(3600L, properties.getExpirationSeconds());
        assertEquals(86400L, properties.getRefreshExpirationSeconds());
        assertEquals("Bearer ", properties.getTokenPrefix());
        assertEquals("Authorization", properties.getHeaderName());
        assertTrue(properties.isEnabled());
        assertEquals("roles", properties.getRolesClaim());
        assertEquals("permissions", properties.getPermissionsClaim());
        assertEquals("sub", properties.getUserIdClaim());
        assertEquals(30L, properties.getClockSkewSeconds());
    }

    @Test
    public void shouldHaveDefaultExcludedPaths() {
        FtgoJwtProperties properties = new FtgoJwtProperties();

        assertNotNull(properties.getExcludedPaths());
        assertEquals(3, properties.getExcludedPaths().size());
        assertTrue(properties.getExcludedPaths().contains("/actuator/health"));
        assertTrue(properties.getExcludedPaths().contains("/actuator/health/**"));
        assertTrue(properties.getExcludedPaths().contains("/actuator/info"));
    }

    @Test
    public void shouldSetAndGetIssuer() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setIssuer("https://custom-issuer.com");
        assertEquals("https://custom-issuer.com", properties.getIssuer());
    }

    @Test
    public void shouldSetAndGetSecret() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setSecret("my-secret-key");
        assertEquals("my-secret-key", properties.getSecret());
    }

    @Test
    public void shouldSetAndGetExpirationSeconds() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setExpirationSeconds(7200L);
        assertEquals(7200L, properties.getExpirationSeconds());
    }

    @Test
    public void shouldSetAndGetRefreshExpirationSeconds() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setRefreshExpirationSeconds(172800L);
        assertEquals(172800L, properties.getRefreshExpirationSeconds());
    }

    @Test
    public void shouldSetAndGetEnabled() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setEnabled(false);
        assertFalse(properties.isEnabled());
    }

    @Test
    public void shouldSetAndGetCustomClaims() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setRolesClaim("realm_roles");
        properties.setPermissionsClaim("scope");
        properties.setUserIdClaim("user_id");

        assertEquals("realm_roles", properties.getRolesClaim());
        assertEquals("scope", properties.getPermissionsClaim());
        assertEquals("user_id", properties.getUserIdClaim());
    }

    @Test
    public void shouldSetAndGetExcludedPaths() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setExcludedPaths(Arrays.asList("/public/**", "/auth/**"));

        assertEquals(2, properties.getExcludedPaths().size());
        assertTrue(properties.getExcludedPaths().contains("/public/**"));
        assertTrue(properties.getExcludedPaths().contains("/auth/**"));
    }

    @Test
    public void shouldReturnExcludedPathsAsArray() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setExcludedPaths(Arrays.asList("/public/**", "/auth/**"));

        String[] pathsArray = properties.getExcludedPathsArray();
        assertNotNull(pathsArray);
        assertEquals(2, pathsArray.length);
        assertArrayEquals(new String[]{"/public/**", "/auth/**"}, pathsArray);
    }

    @Test
    public void shouldSetAndGetClockSkewSeconds() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setClockSkewSeconds(60L);
        assertEquals(60L, properties.getClockSkewSeconds());
    }

    @Test
    public void shouldSetAndGetPublicKeyLocation() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setPublicKeyLocation("classpath:jwt/public-key.pem");
        assertEquals("classpath:jwt/public-key.pem", properties.getPublicKeyLocation());
    }

    @Test
    public void shouldSetAndGetJwksUri() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setJwksUri("https://keycloak.ftgo.com/realms/ftgo/protocol/openid-connect/certs");
        assertEquals("https://keycloak.ftgo.com/realms/ftgo/protocol/openid-connect/certs",
                properties.getJwksUri());
    }

    @Test
    public void shouldSetAndGetTokenPrefixAndHeaderName() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        properties.setTokenPrefix("Token ");
        properties.setHeaderName("X-Auth-Token");
        assertEquals("Token ", properties.getTokenPrefix());
        assertEquals("X-Auth-Token", properties.getHeaderName());
    }
}
